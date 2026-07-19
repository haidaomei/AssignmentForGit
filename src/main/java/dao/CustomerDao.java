package dao;

import entity.Customer;
import entity.TransferLog;

import java.sql.*;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 客户表 DAO，负责客户的查询、保存、逻辑删除、最后跟进时间更新和转移审计。
 *
 * <p>
 * 为了让初学者容易区分层次：Servlet 处理 HTTP，Service 处理业务和事务，本 DAO 只处理 SQL。
 * 所有用户输入都通过问号占位符绑定，没有把输入直接拼到 SQL 中。
 */
public class CustomerDao
{
    /**
     * JdbcTemplate 自动完成查询连接的申请和归还。
     */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /**
     * SELECT 列表公共片段。除了 c.*，还读取关联名称，并计算 warning_days。 从未跟进的客户用 999
     * 表示，排序时自然会被放到预警列表最前面。
     */
    private static final String SELECT = "SELECT c.*,l.level_name,s.source_name,u.real_name" + " owner_name,DATE_FORMAT(c.last_follow_time,'%Y-%m-%d %H:%i')" + " last_follow,DATE_FORMAT(c.create_time,'%Y-%m-%d') created,IF(c.last_follow_time IS" + " NULL,999,DATEDIFF(NOW(),c.last_follow_time)) warning_days ";

    /**
     * LEFT JOIN 允许等级、来源或负责人为空时客户仍能被查出。
     */
    private static final String FROM = " FROM crm_customer c LEFT JOIN crm_customer_level l ON c.level_id=l.id LEFT JOIN" + " crm_lead_source s ON c.source_id=s.id LEFT JOIN sys_user u ON c.owner_user_id=u.id ";

    /**
     * 把 SQL 的一行结果逐项装入 Customer；别名 last_follow、created 对应格式化结果。
     */
    private final RowMapper<Customer> mapper = new RowMapper<Customer>()
    {
        @Override
        public Customer mapRow(ResultSet r, int n) throws SQLException
        {
            Customer c = new Customer();
            c.setId(r.getInt("id"));
            c.setCustomerNo(r.getString("customer_no"));
            c.setCustomerName(r.getString("customer_name"));
            c.setIndustry(r.getString("industry"));
            c.setScale(r.getString("scale"));
            c.setProvince(r.getString("province"));
            c.setCity(r.getString("city"));
            c.setAddress(r.getString("address"));
            c.setWebsite(r.getString("website"));
            c.setLevelId((Integer) r.getObject("level_id"));
            c.setSourceId((Integer) r.getObject("source_id"));
            c.setOwnerUserId((Integer) r.getObject("owner_user_id"));
            c.setStatus(r.getInt("status"));
            c.setCreditRating(r.getString("credit_rating"));
            c.setDescription(r.getString("description"));
            c.setLevelName(r.getString("level_name"));
            c.setSourceName(r.getString("source_name"));
            c.setOwnerName(r.getString("owner_name"));
            c.setLastFollowTime(r.getString("last_follow"));
            c.setCreateTime(r.getString("created"));
            c.setWarningDays(r.getInt("warning_days"));
            return c;
        }
    };

    private String scope(boolean sales)
    {
        // true 表示当前用户是销售员，需要追加负责人条件；false 表示管理员/经理看全部。
        return sales ? " AND c.owner_user_id=? " : "";
    }

    /**
     * 统计满足关键字和权限范围的客户数量。
     */
    public int count(String keyword, Integer userId, boolean sales)
    {
        String sql = "SELECT COUNT(*)" + FROM + " WHERE c.status=1 AND (c.customer_name LIKE ? OR c.customer_no LIKE ?)" + scope(sales);
        // 空关键字会变成 %% 并匹配全部；trim 去掉用户误输入的首尾空格。
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        Integer n = sales ? tpl.queryForObject(sql, Integer.class, k, k, userId) : tpl.queryForObject(sql, Integer.class, k, k);
        return n == null ? 0 : n;
    }

    /**
     * 查询当前页客户。LIMIT 的第一个参数是偏移量，第二个参数是每页条数。
     */
    public List<Customer> page(int offset, int size, String keyword, Integer userId, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE c.status=1 AND (c.customer_name LIKE ? OR c.customer_no LIKE ?)" + scope(sales) + " ORDER BY c.create_time DESC LIMIT ?,?";
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        return sales ? tpl.query(sql, mapper, k, k, userId, offset, size) : tpl.query(sql, mapper, k, k, offset, size);
    }

    /**
     * 查询权限范围内全部客户，供表单下拉框使用。
     */
    public List<Customer> all(Integer userId, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE c.status=1" + scope(sales) + " ORDER BY c.customer_name";
        return sales ? tpl.query(sql, mapper, userId) : tpl.query(sql, mapper);
    }

    /**
     * 按主键查询客户；销售员查询时还必须同时满足 owner_user_id。
     */
    public Customer findById(int id, Integer userId, boolean sales)
    {
        try
        {
            String sql = SELECT + FROM + " WHERE c.status=1 AND c.id=?" + scope(sales);
            return sales ? tpl.queryForObject(sql, mapper, id, userId) : tpl.queryForObject(sql, mapper, id);
        }
        catch (EmptyResultDataAccessException e)
        {
            // 没有记录是正常业务结果，转换为 null 后由上层决定显示 404 或禁止访问。
            return null;
        }
    }

    /**
     * 查询超过 30 天未跟进或从未跟进的有效客户。
     */
    public List<Customer> warnings(Integer userId, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE c.status=1 AND (c.last_follow_time IS NULL OR" + " DATEDIFF(NOW(),c.last_follow_time)>30)" + scope(sales) + " ORDER BY warning_days DESC";
        return sales ? tpl.query(sql, mapper, userId) : tpl.query(sql, mapper);
    }

    /**
     * 在当前事务连接中生成当天客户编号。 例如找到当天最大尾号 0007，就返回 KH + yyyyMMdd + 0008。
     */
    public String nextNumber(Connection conn) throws SQLException
    {
        // 日期前缀每天变化，因此流水号会在新的一天重新从 0001 开始。
        String day = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String p = "KH" + day;
        try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(MAX(CAST(RIGHT(customer_no,4) AS UNSIGNED)),0)+1 FROM crm_customer" + " WHERE customer_no LIKE ?"))
        {
            // LIKE KH20260714% 只参与计算当天的客户编号。
            ps.setString(1, p + "%");
            try (ResultSet rs = ps.executeQuery())
            {
                // 聚合 SQL 一定返回一行；COALESCE 保证表为空时结果为 1。
                rs.next();
                return p + String.format("%04d", rs.getInt(1));
            }
        }
    }

    /**
     * 新增客户。业务编号已由 Service 在同一事务中生成。
     */
    public int save(Connection conn, Customer c) throws SQLException
    {
        String sql = "INSERT INTO" + " crm_customer(customer_no,customer_name,industry,scale,province,city,address,website,level_id,source_id,owner_user_id,credit_rating,description,status)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,1)";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, c, 1, true);
            return p.executeUpdate();
        }
    }

    /**
     * 更新客户可编辑字段；客户编号和创建时间不允许被修改。
     */
    public int update(Connection conn, Customer c) throws SQLException
    {
        String sql = "UPDATE crm_customer SET" + " customer_name=?,industry=?,scale=?,province=?,city=?,address=?,website=?,level_id=?,source_id=?,owner_user_id=?,credit_rating=?,description=?,update_time=NOW()" + " WHERE id=? AND status=1";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, c, 1, false);
            p.setInt(13, c.getId());
            return p.executeUpdate();
        }
    }

    /**
     * 绑定新增/编辑共有字段。
     *
     * @param i
     *            第一个问号序号
     * @param withNo
     *            新增时为 true，需要先绑定 customer_no；编辑时为 false
     */
    private void bind(PreparedStatement p, Customer c, int i, boolean withNo) throws SQLException
    {
        // i++ 表示先使用当前序号，再把序号加 1，正好依次对应 SQL 的问号。
        if (withNo)
        {
            p.setString(i++, c.getCustomerNo());
        }
        p.setString(i++, c.getCustomerName());
        p.setString(i++, c.getIndustry());
        p.setString(i++, c.getScale());
        p.setString(i++, c.getProvince());
        p.setString(i++, c.getCity());
        p.setString(i++, c.getAddress());
        p.setString(i++, c.getWebsite());
        setInt(p, i++, c.getLevelId());
        setInt(p, i++, c.getSourceId());
        setInt(p, i++, c.getOwnerUserId());
        p.setString(i++, c.getCreditRating());
        p.setString(i, c.getDescription());
    }

    /**
     * Integer 为 null 时必须调用 setNull；直接拆箱成 int 会抛 NullPointerException。
     */
    private void setInt(PreparedStatement p, int i, Integer v) throws SQLException
    {
        if (v == null)
        {
            p.setNull(i, Types.INTEGER);
        }
        else
        {
            p.setInt(i, v);
        }
    }

    /**
     * 逻辑删除：status=0 后列表 SQL 的 c.status=1 会自动将其隐藏。
     */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_customer SET status=?,update_time=NOW() WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }

    /**
     * 新增跟进记录后同步更新客户最近跟进时间。
     */
    public int updateLastFollowTime(Connection conn, int id, String time) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_customer SET last_follow_time=?,update_time=NOW() WHERE id=? AND" + " status=1"))
        {
            p.setString(1, time);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }

    /**
     * 把客户负责人更新为新用户；转移日志由另一个方法写入。
     */
    public int transfer(Connection conn, int id, int toUser) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_customer SET owner_user_id=?,update_time=NOW() WHERE id=? AND status=1"))
        {
            p.setInt(1, toUser);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }

    /**
     * 追加一条不可变的客户转移审计记录。
     */
    public int saveTransfer(
            Connection conn, int customerId, Integer fromUser, int toUser, String reason) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("INSERT INTO crm_customer_transfer_log(customer_id,from_user_id,to_user_id,reason)" + " VALUES(?,?,?,?)"))
        {
            p.setInt(1, customerId);
            setInt(p, 2, fromUser);
            p.setInt(3, toUser);
            p.setString(4, reason);
            return p.executeUpdate();
        }
    }

    /**
     * 查询客户的全部转移历史，同时 JOIN 两次用户表得到转出人与转入人姓名。
     */
    public List<TransferLog> transfers(int customerId)
    {
        return tpl.query("SELECT t.*,f.real_name from_name,u.real_name to_name,DATE_FORMAT(t.transfer_time,'%Y-%m-%d" + " %H:%i') transfer_at FROM crm_customer_transfer_log t LEFT JOIN sys_user f ON" + " t.from_user_id=f.id JOIN sys_user u ON t.to_user_id=u.id WHERE t.customer_id=?" + " ORDER BY t.transfer_time DESC", new RowMapper<TransferLog>()
        {
            @Override
            public TransferLog mapRow(ResultSet r, int n) throws SQLException
            {
                TransferLog x = new TransferLog();
                x.setId(r.getInt("id"));
                x.setCustomerId(r.getInt("customer_id"));
                x.setFromUserId((Integer) r.getObject("from_user_id"));
                x.setToUserId(r.getInt("to_user_id"));
                x.setFromUserName(r.getString("from_name"));
                x.setToUserName(r.getString("to_name"));
                x.setTransferTime(r.getString("transfer_at"));
                x.setReason(r.getString("reason"));
                return x;
            }
        }, customerId);
    }
}
