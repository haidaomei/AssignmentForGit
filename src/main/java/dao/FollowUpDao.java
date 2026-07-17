package dao;

import entity.FollowUp;
import java.sql.*;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 跟进记录 DAO。
 *
 * <p>
 * 查询会关联客户、商机、联系人和跟进用户，把页面需要的名称一次性查出。SQL 还计算 overdue， 当前时间超过 next_follow_time
 * 且尚未提醒时值为 1，JSP
 * 据此把该行标黄。
 */
public class FollowUpDao
{
    /** 查询工具。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /** 共用 SELECT 列和计算字段。 */
    private static final String SELECT = "SELECT f.*,c.customer_name,o.title opportunity_title,ct.name contact_name,u.real_name" + " follow_user_name,DATE_FORMAT(f.follow_time,'%Y-%m-%d %H:%i')" + " followed,DATE_FORMAT(f.next_follow_time,'%Y-%m-%d %H:%i')" + " next_follow,DATE_FORMAT(f.create_time,'%Y-%m-%d') created,IF(f.next_follow_time<NOW()" + " AND f.is_reminded=0,1,0) overdue ";

    /** 客户是必选所以 JOIN，商机和联系人是可选所以 LEFT JOIN。 */
    private static final String FROM = " FROM crm_follow_up_record f JOIN crm_customer c ON f.customer_id=c.id LEFT JOIN" + " crm_business_opportunity o ON f.opportunity_id=o.id LEFT JOIN crm_contact ct ON" + " f.contact_id=ct.id LEFT JOIN sys_user u ON f.follow_user_id=u.id ";

    /** 把一行关联查询结果装入 FollowUp。 */
    private final RowMapper<FollowUp> mapper = new RowMapper<FollowUp>()
    {
        @Override
        public FollowUp mapRow(ResultSet r, int n) throws SQLException
        {
            FollowUp x = new FollowUp();
            x.setId(r.getInt("id"));
            x.setCustomerId(r.getInt("customer_id"));
            x.setCustomerName(r.getString("customer_name"));
            x.setOpportunityId((Integer) r.getObject("opportunity_id"));
            x.setOpportunityTitle(r.getString("opportunity_title"));
            x.setContactId((Integer) r.getObject("contact_id"));
            x.setContactName(r.getString("contact_name"));
            x.setFollowType(r.getString("follow_type"));
            x.setFollowContent(r.getString("follow_content"));
            x.setCustomerFeedback(r.getString("customer_feedback"));
            x.setNextPlan(r.getString("next_plan"));
            x.setNextFollowTime(r.getString("next_follow"));
            x.setFollowUserId((Integer) r.getObject("follow_user_id"));
            x.setFollowUserName(r.getString("follow_user_name"));
            x.setFollowTime(r.getString("followed"));
            x.setIsReminded(r.getInt("is_reminded"));
            x.setStatus(r.getInt("status"));
            x.setCreateTime(r.getString("created"));
            x.setOverdue(r.getInt("overdue") == 1);
            return x;
        }
    };

    private String scope(boolean sales)
    {
        // 跟进记录的数据权限跟随客户负责人。
        return sales ? " AND c.owner_user_id=? " : "";
    }

    private String filters(String type, String from, String to)
    {
        // 只拼接固定的筛选片段；具体值仍使用问号绑定。
        StringBuilder s = new StringBuilder();
        if (type != null && !type.isBlank())
        {
            s.append(" AND f.follow_type=?");
        }
        if (from != null && !from.isBlank())
        {
            s.append(" AND DATE(f.follow_time)>=?");
        }
        if (to != null && !to.isBlank())
        {
            s.append(" AND DATE(f.follow_time)<=?");
        }
        return s.toString();
    }

    private Object[] args(
            String type, String from, String to, Integer uid, boolean sales, Object... tail)
    {
        // 参数必须严格按照 filters、scope、LIMIT 在 SQL 中出现的顺序加入。
        java.util.ArrayList<Object> a = new java.util.ArrayList<>();
        if (type != null && !type.isBlank())
        {
            a.add(type);
        }
        if (from != null && !from.isBlank())
        {
            a.add(from);
        }
        if (to != null && !to.isBlank())
        {
            a.add(to);
        }
        if (sales)
        {
            a.add(uid);
        }
        java.util.Collections.addAll(a, tail);
        return a.toArray();
    }

    /** 统计筛选结果总数。 */
    public int count(String type, String from, String to, Integer uid, boolean sales)
    {
        String sql = "SELECT COUNT(*)" + FROM + " WHERE f.status=1 AND c.status=1" + filters(type, from, to) + scope(sales);
        Integer n = tpl.queryForObject(sql, Integer.class, args(type, from, to, uid, sales));
        return n == null ? 0 : n;
    }

    /** 查询跟进记录分页列表。 */
    public List<FollowUp> page(
            int offset, int size, String type, String from, String to, Integer uid, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE f.status=1 AND c.status=1" + filters(type, from, to) + scope(sales) + " ORDER BY f.follow_time DESC LIMIT ?,?";
        return tpl.query(sql, mapper, args(type, from, to, uid, sales, offset, size));
    }

    /** 查询某客户的全部有效跟进，供客户详情时间线使用。 */
    public List<FollowUp> byCustomer(int id)
    {
        return tpl.query(SELECT + FROM + " WHERE f.status=1 AND c.status=1 AND f.customer_id=? ORDER BY f.follow_time DESC", mapper, id);
    }

    /** 查询某商机的全部有效跟进，供商机详情时间线使用。 */
    public List<FollowUp> byOpportunity(int id)
    {
        return tpl.query(SELECT + FROM + " WHERE f.status=1 AND c.status=1 AND f.opportunity_id=? ORDER BY f.follow_time DESC", mapper, id);
    }

    /** 查询最近 limit 条跟进，仪表盘默认传入 5。 */
    public List<FollowUp> latest(Integer uid, boolean sales, int limit)
    {
        String sql = SELECT + FROM + " WHERE f.status=1 AND c.status=1" + scope(sales) + " ORDER BY f.follow_time DESC LIMIT ?";
        return sales ? tpl.query(sql, mapper, uid, limit) : tpl.query(sql, mapper, limit);
    }

    /** 查询计划跟进日期等于今天且尚未提醒的记录。 */
    public List<FollowUp> today(Integer uid, boolean sales)
    {
        String sql = SELECT + FROM + " WHERE f.status=1 AND c.status=1 AND DATE(f.next_follow_time)=CURDATE() AND" + " f.is_reminded=0" + scope(sales) + " ORDER BY f.next_follow_time";
        return sales ? tpl.query(sql, mapper, uid) : tpl.query(sql, mapper);
    }

    /** 插入跟进记录；关联商机、联系人和下次时间都允许为空。 */
    public int save(Connection conn, FollowUp x) throws SQLException
    {
        String sql = "INSERT INTO" + " crm_follow_up_record(customer_id,opportunity_id,contact_id,follow_type,follow_content,customer_feedback,next_plan,next_follow_time,follow_user_id,follow_time,is_reminded,status)" + " VALUES(?,?,?,?,?,?,?,?,?,?,0,1)";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            p.setInt(1, x.getCustomerId());
            setInt(p, 2, x.getOpportunityId());
            setInt(p, 3, x.getContactId());
            p.setString(4, x.getFollowType());
            p.setString(5, x.getFollowContent());
            p.setString(6, x.getCustomerFeedback());
            p.setString(7, x.getNextPlan());
            p.setString(8, blankNull(x.getNextFollowTime()));
            setInt(p, 9, x.getFollowUserId());
            p.setString(10, x.getFollowTime());
            return p.executeUpdate();
        }
    }

    /** 检查当前用户是否有权操作指定跟进记录，用于防止构造 URL 越权删除。 */
    public boolean accessible(int id, Integer uid, boolean sales)
    {
        String sql = "SELECT COUNT(*) FROM crm_follow_up_record f JOIN crm_customer c ON f.customer_id=c.id" + " WHERE f.id=? AND f.status=1 AND c.status=1" + scope(sales);
        Integer n = sales ? tpl.queryForObject(sql, Integer.class, id, uid) : tpl.queryForObject(sql, Integer.class, id);
        return n != null && n > 0;
    }

    /** 把 HTML datetime-local 的空白值转换为数据库 null。 */
    private String blankNull(String s)
    {
        return s == null || s.isBlank() ? null : s;
    }

    /** 绑定可空外键。 */
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

    /** 逻辑删除跟进记录。 */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_follow_up_record SET status=? WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }
}
