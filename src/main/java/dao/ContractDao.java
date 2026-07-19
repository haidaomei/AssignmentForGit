package dao;

import entity.Contract;
import entity.LineItem;
import java.sql.*;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 合同主表和合同产品明细 DAO。
 *
 * <p>
 * 结构与 OpportunityDao 类似：查询使用 JdbcTemplate；新增、编辑、明细替换和逻辑删除均接收 Service 的事务
 * Connection。相似代码保留在各自
 * DAO 中，便于初学者按业务模块阅读。
 */
public class ContractDao
{
    /** 查询工具。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /** 合同查询公共列，并计算 30 天内到期标志 expiring。 */
    private static final String SELECT = "SELECT h.*,c.customer_name,o.title opportunity_title,u.real_name" + " create_user_name,DATE_FORMAT(h.signed_date,'%Y-%m-%d')" + " signed,DATE_FORMAT(h.start_date,'%Y-%m-%d')" + " started,DATE_FORMAT(h.end_date,'%Y-%m-%d')" + " ended,DATE_FORMAT(h.create_time,'%Y-%m-%d') created,IF(h.business_status='执行中' AND" + " h.end_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(),INTERVAL 30 DAY),1,0) expiring ";

    /** 关联客户、可选商机和创建用户。 */
    private static final String FROM = " FROM crm_contract h JOIN crm_customer c ON h.customer_id=c.id LEFT JOIN" + " crm_business_opportunity o ON h.opportunity_id=o.id LEFT JOIN sys_user u ON" + " h.create_user_id=u.id ";

    /** 合同主表映射器。 */
    private final RowMapper<Contract> mapper = new RowMapper<Contract>()
    {
        @Override
        public Contract mapRow(ResultSet r, int n) throws SQLException
        {
            Contract x = new Contract();
            x.setId(r.getInt("id"));
            x.setContractNo(r.getString("contract_no"));
            x.setContractName(r.getString("contract_name"));
            x.setOpportunityId((Integer) r.getObject("opportunity_id"));
            x.setOpportunityTitle(r.getString("opportunity_title"));
            x.setCustomerId(r.getInt("customer_id"));
            x.setCustomerName(r.getString("customer_name"));
            x.setContractAmount(r.getBigDecimal("contract_amount"));
            x.setSignedDate(r.getString("signed"));
            x.setStartDate(r.getString("started"));
            x.setEndDate(r.getString("ended"));
            x.setPaymentTerms(r.getString("payment_terms"));
            x.setBusinessStatus(r.getString("business_status"));
            x.setAttachmentPath(r.getString("attachment_path"));
            x.setCreateUserId((Integer) r.getObject("create_user_id"));
            x.setCreateUserName(r.getString("create_user_name"));
            x.setRemarks(r.getString("remarks"));
            x.setStatus(r.getInt("status"));
            x.setCreateTime(r.getString("created"));
            x.setExpiring(r.getInt("expiring") == 1);
            return x;
        }
    };

    /** 合同产品明细映射器。 */
    private final RowMapper<LineItem> itemMapper = new RowMapper<LineItem>()
    {
        @Override
        public LineItem mapRow(ResultSet r, int n) throws SQLException
        {
            LineItem x = new LineItem();
            x.setId(r.getInt("id"));
            x.setParentId(r.getInt("contract_id"));
            x.setProductId(r.getInt("product_id"));
            x.setProductName(r.getString("product_name"));
            x.setQuantity(r.getInt("quantity"));
            x.setUnitPrice(r.getBigDecimal("unit_price"));
            x.setSubtotal(r.getBigDecimal("subtotal"));
            x.setStatus(r.getInt("status"));
            return x;
        }
    };

    private String scope(boolean sales)
    {
        // 合同数据范围跟随客户负责人：销售员只能看自己客户的合同。
        return sales ? " AND c.owner_user_id=? " : "";
    }

    /** 返回合同编号、合同名称和客户名称的模糊搜索条件。 */
    private String keywordFilter(String keyword)
    {
        return keyword == null || keyword.isBlank() ? "" : " AND (h.contract_no LIKE ? OR h.contract_name LIKE ? OR c.customer_name LIKE ?) ";
    }

    /** 统计权限与关键词搜索后的合同数量。 */
    public int count(String keyword, Integer uid, boolean sales)
    {
        String sql = "SELECT COUNT(*)" + FROM + " WHERE h.status=1 AND c.status=1" + keywordFilter(keyword) + scope(sales);
        java.util.List<Object> a = new java.util.ArrayList<>();
        addKeywordArgs(a, keyword);
        if (sales)
            a.add(uid);
        Integer n = tpl.queryForObject(sql, Integer.class, a.toArray());
        return n == null ? 0 : n;
    }

    /** 按关键词查询合同分页列表。参数列表顺序必须与动态 SQL 问号顺序一致。 */
    public List<Contract> page(int offset, int size, String keyword, Integer uid, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE h.status=1 AND c.status=1" + keywordFilter(keyword) + scope(sales) + " ORDER BY h.create_time DESC LIMIT ?,?";
        java.util.List<Object> a = new java.util.ArrayList<>();
        addKeywordArgs(a, keyword);
        if (sales)
            a.add(uid);
        a.add(offset);
        a.add(size);
        return tpl.query(sql, mapper, a.toArray());
    }

    /** 为合同查询的三个 LIKE 问号追加同一个参数值。 */
    private void addKeywordArgs(java.util.List<Object> args, String keyword)
    {
        if (keyword == null || keyword.isBlank())
            return;
        String like = "%" + keyword.trim() + "%";
        args.add(like);
        args.add(like);
        args.add(like);
    }

    /** 查询合同详情，并装入它的有效产品明细。 */
    public Contract findById(int id, Integer uid, boolean sales)
    {
        try
        {
            String sql = SELECT + FROM + " WHERE h.status=1 AND c.status=1 AND h.id=?" + scope(sales);
            Contract x = sales ? tpl.queryForObject(sql, mapper, id, uid) : tpl.queryForObject(sql, mapper, id);
            if (x != null)
                x.setItems(items(id));
            return x;
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    /** 查询合同产品明细。 */
    public List<LineItem> items(int id)
    {
        return tpl.query("SELECT * FROM crm_contract_product WHERE contract_id=? AND status=1 ORDER BY id", itemMapper, id);
    }

    /** 判断某个成交商机是否已经生成有效合同，防止重复生成。 */
    public boolean existsForOpportunity(int id)
    {
        Integer n = tpl.queryForObject("SELECT COUNT(*) FROM crm_contract WHERE opportunity_id=? AND status=1", Integer.class, id);
        return n != null && n > 0;
    }

    /** 按 HT + 日期 + 四位流水生成合同编号。 */
    public String nextNumber(Connection conn) throws SQLException
    {
        String day = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String p = "HT" + day;
        try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(MAX(CAST(RIGHT(contract_no,4) AS UNSIGNED)),0)+1 FROM crm_contract" + " WHERE contract_no LIKE ?"))
        {
            ps.setString(1, p + "%");
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                return p + String.format("%04d", rs.getInt(1));
            }
        }
    }

    /** 插入合同主表并返回自动生成主键。 */
    public int save(Connection conn, Contract x) throws SQLException
    {
        String sql = "INSERT INTO" + " crm_contract(contract_no,contract_name,opportunity_id,customer_id,contract_amount,signed_date,start_date,end_date,payment_terms,business_status,create_user_id,remarks,status)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,1)";
        try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            bind(p, x, true);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys())
            {
                if (!r.next())
                    throw new SQLException("无法取得合同主键");
                return r.getInt(1);
            }
        }
    }

    /** 更新合同基本信息，合同编号和创建人保持不变。 */
    public int update(Connection conn, Contract x) throws SQLException
    {
        String sql = "UPDATE crm_contract SET" + " contract_name=?,opportunity_id=?,customer_id=?,contract_amount=?,signed_date=?,start_date=?,end_date=?,payment_terms=?,business_status=?,remarks=?,update_time=NOW()" + " WHERE id=? AND status=1";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, x, false);
            p.setInt(11, x.getId());
            return p.executeUpdate();
        }
    }

    /** 绑定合同参数；新增比编辑多绑定合同编号和创建人。 */
    private void bind(PreparedStatement p, Contract x, boolean withNo) throws SQLException
    {
        int i = 1;
        if (withNo)
            p.setString(i++, x.getContractNo());
        p.setString(i++, x.getContractName());
        setInt(p, i++, x.getOpportunityId());
        p.setInt(i++, x.getCustomerId());
        p.setBigDecimal(i++, x.getContractAmount());
        p.setString(i++, blankNull(x.getSignedDate()));
        p.setString(i++, blankNull(x.getStartDate()));
        p.setString(i++, blankNull(x.getEndDate()));
        p.setString(i++, x.getPaymentTerms());
        p.setString(i++, x.getBusinessStatus());
        if (withNo)
            setInt(p, i++, x.getCreateUserId());
        p.setString(i, x.getRemarks());
    }

    /** 空白日期转 null，避免 MySQL 报“Incorrect date value”。 */
    private String blankNull(String s)
    {
        return s == null || s.isBlank() ? null : s;
    }

    /** 安全绑定可为空的商机、创建人外键。 */
    private void setInt(PreparedStatement p, int i, Integer v) throws SQLException
    {
        if (v == null)
            p.setNull(i, Types.INTEGER);
        else
            p.setInt(i, v);
    }

    /** 编辑合同时逻辑停用旧明细。 */
    public int deactivateItems(Connection conn, int id) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_contract_product SET status=0 WHERE contract_id=? AND status=1"))
        {
            p.setInt(1, id);
            return p.executeUpdate();
        }
    }

    /** 从有效产品表读取名称并插入一条合同明细。 */
    public int saveItem(Connection conn, int parentId, LineItem x) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("INSERT INTO" + " crm_contract_product(contract_id,product_id,product_name,quantity,unit_price,subtotal,status)" + " SELECT ?,p.id,p.product_name,?,?,?,1 FROM crm_product p WHERE p.id=? AND" + " p.status=1"))
        {
            p.setInt(1, parentId);
            p.setInt(2, x.getQuantity());
            p.setBigDecimal(3, x.getUnitPrice());
            p.setBigDecimal(4, x.getSubtotal());
            p.setInt(5, x.getProductId());
            return p.executeUpdate();
        }
    }

    /** 逻辑删除合同。 */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_contract SET status=?,update_time=NOW() WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }
}
