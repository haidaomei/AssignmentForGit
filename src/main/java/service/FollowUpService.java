package service;

import dao.CustomerDao;
import dao.FollowUpDao;
import entity.FollowUp;
import entity.PageBean;
import entity.User;
import java.sql.Connection;
import java.util.List;
import util.DbHelper;

/**
 * 跟进记录业务层。
 *
 * <p>
 * 除分页、日期筛选和权限检查外，新增跟进时还会同步更新客户的“最后跟进时间”。两项写操作放在同一事务中， 才能保证预警数据与跟进记录一致。
 */
public class FollowUpService
{
    /** 跟进记录 DAO。 */
    private final FollowUpDao dao = new FollowUpDao();

    /** 用于更新客户的最后跟进时间。 */
    private final CustomerDao customerDao = new CustomerDao();

    /** 按跟进类型和日期区间查询分页，并应用当前用户的数据范围。 */
    public PageBean<FollowUp> page(int p, int size, String type, String from, String to, User u)
    {
        // 修正非法页码和每页大小。
        p = Math.max(1, p);
        size = size <= 0 ? 10 : size;
        int total = dao.count(type, from, to, u.getId(), u.isSales());
        int pages = (int) Math.ceil((double) total / size);
        if (pages > 0 && p > pages)
        {
            p = pages;
        }
        return new PageBean<>(p, size, total, dao.page((p - 1) * size, size, type, from, to, u.getId(), u.isSales()));
    }

    /**
     * @return 指定客户的全部有效跟进记录
     */
    public List<FollowUp> byCustomer(int id)
    {
        return dao.byCustomer(id);
    }

    /**
     * @return 指定商机的全部有效跟进记录
     */
    public List<FollowUp> byOpportunity(int id)
    {
        return dao.byOpportunity(id);
    }

    /**
     * @return 当前用户有权查看的最近 n 条跟进，用于仪表盘
     */
    public List<FollowUp> latest(User u, int n)
    {
        return dao.latest(u.getId(), u.isSales(), n);
    }

    /**
     * @return 当天需要提醒的跟进记录
     */
    public List<FollowUp> today(User u)
    {
        return dao.today(u.getId(), u.isSales());
    }

    /** 判断当前用户是否可以访问某条跟进，用于修改或删除前的权限防护。 */
    public boolean accessible(int id, User u)
    {
        return dao.accessible(id, u.getId(), u.isSales());
    }

    /** 新增跟进记录，同时更新客户最后跟进时间。 */
    public boolean add(FollowUp x)
    {
        // 客户和跟进内容是最低必填项；校验失败时不打开数据库连接。
        if (x == null || x.getCustomerId() == null || x.getFollowContent() == null || x.getFollowContent().isBlank())
        {
            return false;
        }
        Connection conn = null;
        try
        {
            // 1. 从连接池取得连接。
            conn = DbHelper.getDataSource().getConnection();
            // 2. 关闭自动提交。
            conn.setAutoCommit(false);
            // 3. 用同一连接保存跟进，并更新客户时间。
            dao.save(conn, x);
            customerDao.updateLastFollowTime(conn, x.getCustomerId(), x.getFollowTime());
            // 4. 两条 SQL 都成功后提交。
            conn.commit();
            return true;
        }
        catch (Exception e)
        {
            // 5. 失败时回滚两条 SQL。
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
            // 6. 归还连接。
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

    /** 逻辑删除跟进记录，即把 status 更新为 0。 */
    public boolean delete(int id)
    {
        Connection conn = null;
        try
        {
            // 1、2. 取连接并开启手动事务。
            conn = DbHelper.getDataSource().getConnection();
            conn.setAutoCommit(false);
            int n = dao.updateStatus(conn, id, 0);
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 5. 异常回滚。
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
            // 6. 关闭连接。
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
}
// 根据总条数计算总页数，并防止页码越界。
// 3. 执行逻辑删除。
// 4. 提交。
