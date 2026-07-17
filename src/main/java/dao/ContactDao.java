package dao;

import entity.Contact;
import java.sql.*;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 联系人表 DAO。
 *
 * <p>
 * 所有查询都 JOIN 客户表并同时检查 ct.status=1 与 c.status=1。这样客户被逻辑删除后， 即使联系人自身仍是
 * 1，也会按照“父记录删除后子记录级联隐藏”的规则从页面消失。
 */
public class ContactDao
{
    /** 只读查询工具。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /** 共用 SELECT：关联客户名称，并把创建时间格式化成字符串。 */
    private static final String SELECT = "SELECT ct.*,c.customer_name,DATE_FORMAT(ct.create_time,'%Y-%m-%d') created FROM crm_contact" + " ct JOIN crm_customer c ON ct.customer_id=c.id ";

    /** 显式 RowMapper，逐列说明数据库结果如何装入 Contact。 */
    private final RowMapper<Contact> mapper = new RowMapper<Contact>()
    {
        @Override
        public Contact mapRow(ResultSet r, int n) throws SQLException
        {
            Contact x = new Contact();
            x.setId(r.getInt("id"));
            x.setCustomerId(r.getInt("customer_id"));
            x.setName(r.getString("name"));
            x.setCustomerName(r.getString("customer_name"));
            x.setGender(r.getString("gender"));
            x.setPosition(r.getString("position"));
            x.setPhone(r.getString("phone"));
            x.setEmail(r.getString("email"));
            x.setWechat(r.getString("wechat"));
            x.setIsPrimary(r.getInt("is_primary"));
            x.setIsDecisionMaker(r.getInt("is_decision_maker"));
            x.setHobby(r.getString("hobby"));
            x.setRemarks(r.getString("remarks"));
            x.setStatus(r.getInt("status"));
            x.setCreateTime(r.getString("created"));
            return x;
        }
    };

    private String scope(boolean sales)
    {
        // 销售员只能看到自己负责客户的联系人；管理员/经理不追加这个条件。
        return sales ? " AND c.owner_user_id=? " : "";
    }

    /** 统计搜索结果数量；姓名和电话都支持模糊匹配。 */
    public int count(String keyword, Integer uid, boolean sales)
    {
        String sql = "SELECT COUNT(*) FROM crm_contact ct JOIN crm_customer c ON ct.customer_id=c.id WHERE" + " ct.status=1 AND c.status=1 AND (ct.name LIKE ? OR ct.phone LIKE ?)" + scope(sales);
        String k = "%" + (keyword == null ? "" : keyword) + "%";
        Integer n = sales ? tpl.queryForObject(sql, Integer.class, k, k, uid) : tpl.queryForObject(sql, Integer.class, k, k);
        return n == null ? 0 : n;
    }

    /** 查询联系人分页数据，sales=true 时 SQL 会多绑定一个当前用户主键。 */
    public List<Contact> page(int offset, int size, String keyword, Integer uid, boolean sales)
    {
        String sql = SELECT + "WHERE ct.status=1 AND c.status=1 AND (ct.name LIKE ? OR ct.phone LIKE ?)" + scope(sales) + " ORDER BY ct.create_time DESC LIMIT ?,?";
        String k = "%" + (keyword == null ? "" : keyword) + "%";
        return sales ? tpl.query(sql, mapper, k, k, uid, offset, size) : tpl.query(sql, mapper, k, k, offset, size);
    }

    /** 查询某个有效客户下的全部有效联系人，主联系人排在最前面。 */
    public List<Contact> byCustomer(int customerId)
    {
        return tpl.query(SELECT + "WHERE ct.status=1 AND c.status=1 AND ct.customer_id=? ORDER BY ct.is_primary" + " DESC,ct.name", mapper, customerId);
    }

    /** 按主键查询联系人，并继续应用销售员数据范围条件。 */
    public Contact findById(int id, Integer uid, boolean sales)
    {
        try
        {
            String sql = SELECT + "WHERE ct.status=1 AND c.status=1 AND ct.id=?" + scope(sales);
            return sales ? tpl.queryForObject(sql, mapper, id, uid) : tpl.queryForObject(sql, mapper, id);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    /** 新增联系人。Connection 来自 Service，保证它参与 Service 控制的事务。 */
    public int save(Connection conn, Contact x) throws SQLException
    {
        String sql = "INSERT INTO" + " crm_contact(customer_id,name,gender,position,phone,email,wechat,is_primary,is_decision_maker,hobby,remarks,status)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,1)";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, x);
            return p.executeUpdate();
        }
    }

    /** 更新联系人；WHERE status=1 防止修改已经逻辑删除的数据。 */
    public int update(Connection conn, Contact x) throws SQLException
    {
        String sql = "UPDATE crm_contact SET" + " customer_id=?,name=?,gender=?,position=?,phone=?,email=?,wechat=?,is_primary=?,is_decision_maker=?,hobby=?,remarks=?,update_time=NOW()" + " WHERE id=? AND status=1";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, x);
            p.setInt(12, x.getId());
            return p.executeUpdate();
        }
    }

    /** 按问号顺序绑定联系人字段；复选框在实体中已转换成 0 或 1。 */
    private void bind(PreparedStatement p, Contact x) throws SQLException
    {
        p.setInt(1, x.getCustomerId());
        p.setString(2, x.getName());
        p.setString(3, x.getGender());
        p.setString(4, x.getPosition());
        p.setString(5, x.getPhone());
        p.setString(6, x.getEmail());
        p.setString(7, x.getWechat());
        p.setInt(8, x.getIsPrimary() == null ? 0 : x.getIsPrimary());
        p.setInt(9, x.getIsDecisionMaker() == null ? 0 : x.getIsDecisionMaker());
        p.setString(10, x.getHobby());
        p.setString(11, x.getRemarks());
    }

    /** 逻辑删除或恢复联系人。 */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_contact SET status=?,update_time=NOW() WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }
}
