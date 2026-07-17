package dao;

import entity.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 用户表的数据访问对象（DAO，Data Access Object）。
 *
 * <p>
 * DAO 的职责只有“与数据库交谈”，不决定页面跳转，也不决定事务何时提交。查询使用 JdbcTemplate， 新增用户接收 Service 传入的
 * Connection，从而服从
 * Service 的统一事务控制。
 */
public class UserDao
{
    /** 查询工具，内部使用 DbHelper 创建的同一个 Druid 连接池。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /**
     * RowMapper 负责把 ResultSet 当前行转换为 User 对象。 ResultSet 的列名来自 SQL，setter 把每一列写入对应
     * Java 字段。
     */
    private final RowMapper<User> mapper = new RowMapper<User>()
    {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException
        {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setRealName(rs.getString("real_name"));
            u.setPhone(rs.getString("phone"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));
            u.setStatus(rs.getInt("status"));
            return u;
        }
    };

    public User findForLogin(String username, String password)
    {
        // 两个问号是参数占位符，真实输入由 JdbcTemplate 绑定，可防止 SQL 注入。
        try
        {
            return tpl.queryForObject("SELECT * FROM sys_user WHERE username=? AND password=? AND status=1", mapper, username, password);
        }
        catch (EmptyResultDataAccessException e)
        {
            // queryForObject 查不到记录会抛此异常；登录业务更适合用 null 表示“账号不匹配”。
            return null;
        }
    }

    /** 注册前检查用户名是否已经存在。COUNT(*) 只返回一个数字，因此使用 queryForObject。 */
    public boolean exists(String username)
    {
        Integer n = tpl.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username=?", Integer.class, username);
        return n != null && n > 0;
    }

    /** 查询所有启用用户，供负责人下拉框使用。 */
    public List<User> findAllActive()
    {
        return tpl.query("SELECT * FROM sys_user WHERE status=1 ORDER BY role,real_name", mapper);
    }

    /**
     * 插入一个销售员账号。
     *
     * @param conn
     *            Service 已关闭自动提交的事务连接，本方法绝不能自行创建连接
     * @param u
     *            表单转换得到的用户对象
     * @return 数据库受影响行数，正常新增应为 1
     */
    public int save(Connection conn, User u) throws SQLException
    {
        String sql = "INSERT INTO sys_user(username,password,real_name,phone,email,role,status)" + " VALUES(?,?,?,?,?,'sales',1)";
        // try-with-resources 自动关闭 PreparedStatement，但不会关闭外部传入的 Connection。
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            // setString 的序号从 1 开始，必须与 SQL 中问号从左到右的顺序完全一致。
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getRealName());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getEmail());
            return ps.executeUpdate();
        }
    }
}
