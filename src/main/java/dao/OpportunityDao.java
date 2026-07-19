package dao;

import entity.LineItem;
import entity.Opportunity;
import java.sql.*;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 商机主表和商机产品明细 DAO。
 *
 * <p>
 * 主从表必须在一个事务中保存：先插入商机取得自动生成主键，再把该主键写入每一条产品明细。 编辑时不物理删除旧明细，而是先把旧明细 status 改为
 * 0，再插入新的有效明细。
 */
public class OpportunityDao
{
    /** 查询使用的 JdbcTemplate。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /** 商机查询公共列，包含客户、联系人、阶段、负责人和格式化日期。 */
    private static final String SELECT = "SELECT o.*,c.customer_name,ct.name contact_name,st.stage_name,st.sort_order" + " stage_sort,u.real_name owner_name,DATE_FORMAT(o.estimated_close_date,'%Y-%m-%d')" + " close_date,DATE_FORMAT(o.create_time,'%Y-%m-%d') created ";

    /** 商机列表所需的关联表。客户和阶段必有，因此 INNER JOIN；联系人和负责人可空，因此 LEFT JOIN。 */
    private static final String FROM = " FROM crm_business_opportunity o JOIN crm_customer c ON o.customer_id=c.id LEFT JOIN" + " crm_contact ct ON o.contact_id=ct.id JOIN crm_opportunity_stage st ON" + " o.stage_id=st.id LEFT JOIN sys_user u ON o.owner_user_id=u.id ";

    /** 把商机主表关联查询结果映射成 Opportunity。 */
    private final RowMapper<Opportunity> mapper = new RowMapper<Opportunity>()
    {
        @Override
        public Opportunity mapRow(ResultSet r, int n) throws SQLException
        {
            Opportunity x = new Opportunity();
            x.setId(r.getInt("id"));
            x.setOpportunityNo(r.getString("opportunity_no"));
            x.setTitle(r.getString("title"));
            x.setCustomerId(r.getInt("customer_id"));
            x.setCustomerName(r.getString("customer_name"));
            x.setContactId((Integer) r.getObject("contact_id"));
            x.setContactName(r.getString("contact_name"));
            x.setStageId(r.getInt("stage_id"));
            x.setStageName(r.getString("stage_name"));
            x.setStageSort(r.getInt("stage_sort"));
            x.setExpectedAmount(r.getBigDecimal("expected_amount"));
            x.setEstimatedCloseDate(r.getString("close_date"));
            x.setOwnerUserId((Integer) r.getObject("owner_user_id"));
            x.setOwnerName(r.getString("owner_name"));
            x.setProbability(r.getInt("probability"));
            x.setDescription(r.getString("description"));
            x.setResultReason(r.getString("result_reason"));
            x.setBusinessStatus(r.getString("business_status"));
            x.setStatus(r.getInt("status"));
            x.setCreateTime(r.getString("created"));
            return x;
        }
    };

    /** 把一条商机产品明细映射成通用 LineItem。 */
    private final RowMapper<LineItem> itemMapper = new RowMapper<LineItem>()
    {
        @Override
        public LineItem mapRow(ResultSet r, int n) throws SQLException
        {
            LineItem x = new LineItem();
            x.setId(r.getInt("id"));
            x.setParentId(r.getInt("opportunity_id"));
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
        // 销售员只看自己负责的商机，管理角色不追加限制。
        return sales ? " AND o.owner_user_id=? " : "";
    }

    /**
     * 根据关键词追加商机编号、标题和客户名称模糊搜索条件。
     *
     * <p>
     * 这里只拼接预先写好的 SQL 片段，用户输入不会被拼入 SQL，而是稍后通过问号参数绑定。
     */
    private String keywordFilter(String keyword)
    {
        return keyword == null || keyword.isBlank() ? "" : " AND (o.opportunity_no LIKE ? OR o.title LIKE ? OR c.customer_name LIKE ?) ";
    }

    /** 按“三个 LIKE→权限用户→分页”的 SQL 顺序组装参数。 */
    private Object[] args(String keyword, Integer uid, boolean sales, Object... tail)
    {
        java.util.ArrayList<Object> a = new java.util.ArrayList<>();
        if (keyword != null && !keyword.isBlank())
        {
            // 前后的 % 表示关键词可以出现在字段任意位置，三个问号绑定同一模糊值。
            String like = "%" + keyword.trim() + "%";
            a.add(like);
            a.add(like);
            a.add(like);
        }
        if (sales)
            a.add(uid);
        // tail 常用于追加分页的 offset 和 size。
        java.util.Collections.addAll(a, tail);
        return a.toArray();
    }

    /** 统计模糊搜索后的商机总数。客户 status=1 实现父客户删除后的级联隐藏。 */
    public int count(String keyword, Integer uid, boolean sales)
    {
        String sql = "SELECT COUNT(*)" + FROM + " WHERE o.status=1 AND c.status=1" + keywordFilter(keyword) + scope(sales);
        Integer n = tpl.queryForObject(sql, Integer.class, args(keyword, uid, sales));
        return n == null ? 0 : n;
    }

    /** 按关键词查询商机分页列表。 */
    public List<Opportunity> page(int offset, int size, String keyword, Integer uid, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE o.status=1 AND c.status=1" + keywordFilter(keyword) + scope(sales) + " ORDER BY o.create_time DESC LIMIT ?,?";
        return tpl.query(sql, mapper, args(keyword, uid, sales, offset, size));
    }

    /** 查询某个客户的商机，供客户详情页展示。 */
    public List<Opportunity> byCustomer(int customerId)
    {
        return tpl.query(SELECT + FROM + " WHERE o.status=1 AND c.status=1 AND o.customer_id=? ORDER BY o.create_time DESC", mapper, customerId);
    }

    /** 查询商机详情，并额外查询它的有效产品明细放入 items。 */
    public Opportunity findById(int id, Integer uid, boolean sales)
    {
        try
        {
            String sql = SELECT + FROM + " WHERE o.status=1 AND c.status=1 AND o.id=?" + scope(sales);
            Opportunity x = sales ? tpl.queryForObject(sql, mapper, id, uid) : tpl.queryForObject(sql, mapper, id);
            // 主表和从表分两次查询，代码更清晰，也避免 JOIN 后主表数据被每条明细重复。
            if (x != null)
                x.setItems(items(id));
            return x;
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    /** 查询商机有效产品明细。 */
    public List<LineItem> items(int id)
    {
        return tpl.query("SELECT * FROM crm_opportunity_product WHERE opportunity_id=? AND status=1 ORDER BY id", itemMapper, id);
    }

    /** 按 CRM + 日期 + 四位流水生成下一个商机业务编号。 */
    public String nextNumber(Connection conn) throws SQLException
    {
        String day = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String p = "CRM" + day;
        try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(MAX(CAST(RIGHT(opportunity_no,4) AS UNSIGNED)),0)+1 FROM" + " crm_business_opportunity WHERE opportunity_no LIKE ?"))
        {
            ps.setString(1, p + "%");
            try (ResultSet rs = ps.executeQuery())
            {
                rs.next();
                return p + String.format("%04d", rs.getInt(1));
            }
        }
    }

    /** 插入商机主表并返回数据库生成的内部主键。 */
    public int save(Connection conn, Opportunity x) throws SQLException
    {
        String sql = "INSERT INTO" + " crm_business_opportunity(opportunity_no,title,customer_id,contact_id,stage_id,expected_amount,estimated_close_date,owner_user_id,probability,description,result_reason,business_status,status)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,'进行中',1)";
        // RETURN_GENERATED_KEYS 告诉 JDBC：执行后需要取回 AUTO_INCREMENT 生成的 id。
        try (PreparedStatement p = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            bind(p, x, true);
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys())
            {
                if (!r.next())
                    throw new SQLException("无法取得商机主键");
                return r.getInt(1);
            }
        }
    }

    /** 更新商机基本信息，不直接修改业务状态；业务状态由阶段推进方法维护。 */
    public int update(Connection conn, Opportunity x) throws SQLException
    {
        String sql = "UPDATE crm_business_opportunity SET" + " title=?,customer_id=?,contact_id=?,stage_id=?,expected_amount=?,estimated_close_date=?,owner_user_id=?,probability=?,description=?,result_reason=?,update_time=NOW()" + " WHERE id=? AND status=1";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, x, false);
            p.setInt(11, x.getId());
            return p.executeUpdate();
        }
    }

    /** 绑定商机新增/编辑共有参数，withNo 控制是否包含新增时才有的业务编号。 */
    private void bind(PreparedStatement p, Opportunity x, boolean withNo) throws SQLException
    {
        int i = 1;
        if (withNo)
            p.setString(i++, x.getOpportunityNo());
        p.setString(i++, x.getTitle());
        p.setInt(i++, x.getCustomerId());
        setInt(p, i++, x.getContactId());
        p.setInt(i++, x.getStageId());
        p.setBigDecimal(i++, x.getExpectedAmount());
        p.setString(i++, blankNull(x.getEstimatedCloseDate()));
        setInt(p, i++, x.getOwnerUserId());
        p.setInt(i++, x.getProbability() == null ? 0 : x.getProbability());
        p.setString(i++, x.getDescription());
        p.setString(i, x.getResultReason());
    }

    /** 把空白字符串转为 null，数据库日期列不能接收空字符串。 */
    private String blankNull(String s)
    {
        return s == null || s.isBlank() ? null : s;
    }

    /** 安全绑定可空的 Integer 外键。 */
    private void setInt(PreparedStatement p, int i, Integer v) throws SQLException
    {
        if (v == null)
            p.setNull(i, Types.INTEGER);
        else
            p.setInt(i, v);
    }

    /** 编辑主从表时先把全部旧明细逻辑删除。 */
    public int deactivateItems(Connection conn, int id) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_opportunity_product SET status=0 WHERE opportunity_id=? AND status=1"))
        {
            p.setInt(1, id);
            return p.executeUpdate();
        }
    }

    /** 插入一条新明细。使用 INSERT ... SELECT 从产品表读取可信产品名称，并确保产品仍处于上架状态。 */
    public int saveItem(Connection conn, int parentId, LineItem x) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("INSERT INTO" + " crm_opportunity_product(opportunity_id,product_id,product_name,quantity,unit_price,subtotal,status)" + " SELECT ?,p.id,p.product_name,?,?,?,1 FROM crm_product p WHERE p.id=? AND" + " p.status=1"))
        {
            p.setInt(1, parentId);
            p.setInt(2, x.getQuantity());
            p.setBigDecimal(3, x.getUnitPrice());
            p.setBigDecimal(4, x.getSubtotal());
            p.setInt(5, x.getProductId());
            return p.executeUpdate();
        }
    }

    /** 推进阶段，同时更新概率、业务状态和成交/丢单原因。 */
    public int advance(
            Connection conn, int id, int stageId, int probability, String businessStatus, String reason) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_business_opportunity SET" + " stage_id=?,probability=?,business_status=?,result_reason=?,update_time=NOW()" + " WHERE id=? AND status=1"))
        {
            p.setInt(1, stageId);
            p.setInt(2, probability);
            p.setString(3, businessStatus);
            p.setString(4, reason);
            p.setInt(5, id);
            return p.executeUpdate();
        }
    }

    /** 逻辑删除商机。 */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_business_opportunity SET status=?,update_time=NOW() WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }
}
