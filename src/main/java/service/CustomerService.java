package service;

import dao.ContactDao;
import dao.CustomerDao;
import dao.FollowUpDao;
import dao.OpportunityDao;
import entity.*;
import java.sql.Connection;
import java.util.List;
import util.DbHelper;

/**
 * 客户模块业务层。
 *
 * <p>
 * 负责分页边界校正、销售员数据权限、客户编号生成、逻辑删除和客户转移。所有写操作都显式执行完整的 事务流程：取连接→关闭自动提交→执行
 * DAO→提交→异常回滚→关闭连接。
 */
public class CustomerService
{
    /** 客户主数据 DAO。 */
    private final CustomerDao dao = new CustomerDao();

    /** 用于查询客户下的联系人。 */
    private final ContactDao contactDao = new ContactDao();

    /** 用于查询客户下的商机。 */
    private final OpportunityDao opportunityDao = new OpportunityDao();

    /** 用于查询客户下的跟进记录。 */
    private final FollowUpDao followUpDao = new FollowUpDao();

    /** 查询客户分页；页码至少为 1，每页数非法时回退为 10。 */
    public PageBean<Customer> page(int page, int size, String keyword, User user)
    {
        // Math.max 保证 page 不会出现 0 或负数。
        page = Math.max(1, page);
        // 调用者没有给出合法每页数时，使用需求规定的 10 条。
        size = size <= 0 ? 10 : size;
        // 销售员只能查看自己负责的客户；管理员/经理可查看全部。
        boolean sales = user.isSales();
        int total = dao.count(keyword, user.getId(), sales);
        // 向上取整，例如 21 条、每页 10 条，一共是 3 页。
        int pages = (int) Math.ceil((double) total / size);
        // 删除最后一页数据后，把过大的页码拉回到最后一页。
        if (pages > 0 && page > pages)
            page = pages;
        return new PageBean<>(page, size, total, dao.page((page - 1) * size, size, keyword, user.getId(), sales));
    }

    /** 查询当前用户有权查看的所有客户，常用于表单下拉框。 */
    public List<Customer> all(User user)
    {
        return dao.all(user.getId(), user.isSales());
    }

    /** 按主键查询一个客户，DAO 同时校验当前用户的数据权限。 */
    public Customer get(int id, User user)
    {
        return dao.findById(id, user.getId(), user.isSales());
    }

    /** 查询超过 30 天未跟进的客户预警列表。 */
    public List<Customer> warnings(User user)
    {
        return dao.warnings(user.getId(), user.isSales());
    }

    /**
     * @return 指定客户下未删除的联系人
     */
    public List<Contact> contacts(int customerId)
    {
        return contactDao.byCustomer(customerId);
    }

    /**
     * @return 指定客户下未删除的商机
     */
    public List<Opportunity> opportunities(int customerId)
    {
        return opportunityDao.byCustomer(customerId);
    }

    /**
     * @return 指定客户下未删除的跟进记录
     */
    public List<FollowUp> follows(int customerId)
    {
        return followUpDao.byCustomer(customerId);
    }

    /**
     * @return 指定客户的负责人转移审计记录
     */
    public List<TransferLog> transfers(int customerId)
    {
        return dao.transfers(customerId);
    }

    /**
     * 新增客户。客户编号和客户记录必须在同一事务中生成，以减少并发产生重复流水号的风险。
     *
     * @return true 表示已提交，false 表示校验失败或数据库异常
     */
    public boolean add(Customer x)
    {
        // 业务层先阻止空对象或空客户名称进入数据库。
        if (x == null || blank(x.getCustomerName()))
            return false;
        Connection conn = null;
        try
        {
            // 事务第 1 步：从 Druid 连接池借用一个数据库连接。
            conn = DbHelper.getDataSource().getConnection();
            // 事务第 2 步：关闭每条 SQL 自动提交，后续 SQL 才能作为一个整体。
            conn.setAutoCommit(false);
            // 事务第 3 步：用同一个 conn 生成编号并保存客户。
            x.setCustomerNo(dao.nextNumber(conn));
            int n = dao.save(conn, x);
            // 事务第 4 步：所有 SQL 都成功后永久保存修改。
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 事务第 5 步：任何一步失败都撤销本次事务已执行的 SQL。
            if (conn != null)
                try
                {
                    conn.rollback();
                }
                catch (Exception ignored)
                {
                }
            e.printStackTrace();
            return false;
        }
        finally
        {
            // 事务第 6 步：无论成功还是失败都归还连接，防止连接池耗尽。
            if (conn != null)
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
        }
    }

    /** 修改客户基本资料；主键、客户名称必须有效。 */
    public boolean update(Customer x)
    {
        if (x == null || x.getId() == null || blank(x.getCustomerName()))
            return false;
        Connection conn = null;
        try
        {
            // 1. 取连接。
            conn = DbHelper.getDataSource().getConnection();
            // 2. 关闭自动提交。
            conn.setAutoCommit(false);
            // 3. 在该连接中执行参数化 UPDATE，并记录受影响行数。
            int n = dao.update(conn, x);
            // 4. 提交事务。
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 5. 异常时回滚。
            if (conn != null)
                try
                {
                    conn.rollback();
                }
                catch (Exception ignored)
                {
                }
            e.printStackTrace();
            return false;
        }
        finally
        {
            // 6. 关闭/归还连接。
            if (conn != null)
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
        }
    }

    /** 逻辑删除客户：只把 status 改为 0，不会物理删除历史数据。 */
    public boolean delete(int id)
    {
        Connection conn = null;
        try
        {
            // 1、2. 获取连接并开启手动事务。
            conn = DbHelper.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // 3. status=0 表示记录已删除；后续查询会自动过滤它及关联数据。
            int n = dao.updateStatus(conn, id, 0);
            // 4. 提交。
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 5. 回滚。
            if (conn != null)
                try
                {
                    conn.rollback();
                }
                catch (Exception ignored)
                {
                }
            e.printStackTrace();
            return false;
        }
        finally
        {
            // 6. 关闭连接。
            if (conn != null)
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
        }
    }

    /**
     * 转移客户负责人。修改客户的负责人和写入审计日志必须同时成功或同时失败。
     *
     * @param id
     *            客户主键
     * @param toUser
     *            新负责人的用户主键
     * @param reason
     *            转移原因，会保存到审计表
     */
    public boolean transfer(int id, int toUser, String reason)
    {
        // 将只读查询放在事务连接获取之前，避免在事务持有期间占用独立连接。
        Customer old = dao.findById(id, null, false);
        if (old == null)
            return false;
        Connection conn = null;
        try
        {
            // 1、2. 取连接并开启手动事务。
            conn = DbHelper.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // 3. 修改客户负责人，并把前后负责人写入审计日志。
            dao.transfer(conn, id, toUser);
            dao.saveTransfer(conn, id, old.getOwnerUserId(), toUser, reason);
            // 4. 两项写操作全部成功后再提交。
            conn.commit();
            return true;
        }
        catch (Exception e)
        {
            // 5. 任一写操作失败时回滚，避免出现“负责人改了但日志没写”。
            if (conn != null)
                try
                {
                    conn.rollback();
                }
                catch (Exception ignored)
                {
                }
            e.printStackTrace();
            return false;
        }
        finally
        {
            // 6. 关闭连接。
            if (conn != null)
                try
                {
                    conn.close();
                }
                catch (Exception ignored)
                {
                }
        }
    }

    /** 判断字符串是否为 null、空字符串或只包含空白字符。 */
    private boolean blank(String s)
    {
        return s == null || s.trim().isEmpty();
    }
}
