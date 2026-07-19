package service;

import dao.ContactDao;
import entity.Contact;
import entity.PageBean;
import entity.User;

import java.sql.Connection;
import java.util.List;

import util.DbHelper;

/**
 * 联系人业务层：负责分页边界、必填校验、事务保存和逻辑删除。
 */
public class ContactService
{
    /**
     * 所有数据库操作委托给 ContactDao。
     */
    private final ContactDao dao = new ContactDao();

    /**
     * 规范页码后查询一页联系人，并把总数与数据封装成 PageBean。
     */
    public PageBean<Contact> page(int p, int size, String keyword, User u)
    {
        // 页码最小为 1；每页条数非法时恢复为需求规定的 10。
        p = Math.max(1, p);
        size = size <= 0 ? 10 : size;
        int total = dao.count(keyword, u.getId(), u.isSales());
        int pages = (int) Math.ceil((double) total / size);
        // 请求页码超过末页时回到最后一页，避免出现“有数据但页面空白”。
        if (pages > 0 && p > pages)
        {
            p = pages;
        }
        return new PageBean<>(p, size, total, dao.page((p - 1) * size, size, keyword, u.getId(), u.isSales()));
    }

    /**
     * 查询某客户联系人，供联动下拉框和客户详情页使用。
     */
    public List<Contact> byCustomer(int id)
    {
        return dao.byCustomer(id);
    }

    /**
     * 按当前用户权限查询联系人详情。
     */
    public Contact get(int id, User u)
    {
        return dao.findById(id, u.getId(), u.isSales());
    }

    /**
     * 新增或编辑联系人。id 为空表示新增，有 id 表示编辑。 两种操作都属于写操作，因此共享同一套完整事务模板。
     */
    public boolean save(Contact x)
    {
        // 所属客户和姓名是数据库必填项，先在业务层给出失败结果。
        if (x == null || x.getCustomerId() == null || x.getName() == null || x.getName().isBlank())
        {
            return false;
        }
        Connection conn = null;
        try
        {
            // 事务步骤 1：获取连接。
            conn = DbHelper.getDataSource().getConnection();
            // 事务步骤 2：关闭自动提交。
            conn.setAutoCommit(false);
            // 事务步骤 3：根据是否存在主键选择 INSERT 或 UPDATE，并始终传同一个 conn。
            int n = x.getId() == null ? dao.save(conn, x) : dao.update(conn, x);
            // 事务步骤 4：提交。
            conn.commit();
            // executeUpdate 返回受影响行数，大于 0 才说明真正保存成功。
            return n > 0;
        }
        catch (Exception e)
        {
            // 事务步骤 5：异常回滚。
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
            // 事务步骤 6：归还连接。
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
     * 把联系人 status 改为 0；不物理删除记录。
     */
    public boolean delete(int id)
    {
        Connection conn = null;
        try
        {
            // 事务步骤 1、2：取得连接并关闭自动提交。
            conn = DbHelper.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // 事务步骤 3：逻辑删除。
            int n = dao.updateStatus(conn, id, 0);
            // 事务步骤 4：提交。
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 事务步骤 5：失败回滚。
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
            // 事务步骤 6：关闭/归还连接。
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
