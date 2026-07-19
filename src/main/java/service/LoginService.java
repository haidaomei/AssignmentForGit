package service;

import dao.UserDao;
import entity.User;

import java.sql.Connection;

import util.DbHelper;

/**
 * 登录与注册业务层。
 *
 * <p>
 * 登录是只读操作，直接调用 DAO；注册会写数据库，所以必须完整执行手动事务六步骤。
 */
public class LoginService
{
    /**
     * 业务层持有 DAO，通过它访问 sys_user 表。
     */
    private final UserDao dao = new UserDao();

    /**
     * 校验非空后查询账号；成功返回 User，失败返回 null。
     */
    public User login(String username, String password)
    {
        // 提前返回可以避免无意义的数据库查询。
        if (blank(username) || blank(password))
        {
            return null;
        }
        // 用户名去掉首尾空格，密码不能 trim，因为空格可能本来就是密码的一部分。
        return dao.findForLogin(username.trim(), password);
    }

    /**
     * 注册新销售员。任何一步失败都回滚，保证不会留下半条数据。
     */
    public boolean register(User user)
    {
        // 依次检查对象、必填项、密码长度和用户名唯一性；不满足要求时不访问写接口。
        if (user == null || blank(user.getUsername()) || blank(user.getPassword()) || user.getPassword().length() < 6 || blank(user.getRealName()) || dao.exists(user.getUsername()))
        {
            return false;
        }
        Connection conn = null;
        try
        {
            // 事务步骤 1：从 Druid 连接池借出一条数据库连接。
            conn = DbHelper.getDataSource().getConnection();
            // 事务步骤 2：关闭自动提交，后面的 SQL 不会执行一条就立即永久生效。
            conn.setAutoCommit(false);
            // 事务步骤 3：DAO 必须使用同一个 conn 执行 INSERT。
            int n = dao.save(conn, user);
            // 事务步骤 4：全部成功后提交，注册数据才正式生效。
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 事务步骤 5：任意异常都撤销本事务已经执行的 SQL。
            if (conn != null)
            {
                try
                {
                    conn.rollback();
                }
                catch (Exception ignored)
                {
                }
            }
            e.printStackTrace();
            return false;
        }
        finally
        {
            // 事务步骤 6：无论成功失败都关闭连接；对连接池而言是“归还连接”。
            if (conn != null)
            {
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
            }
        }
    }

    /**
     * 同时识别 null、空串以及只包含空格的字符串。
     */
    private boolean blank(String s)
    {
        return s == null || s.trim().isEmpty();
    }
}
