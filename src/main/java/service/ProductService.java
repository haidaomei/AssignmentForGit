package service;

import dao.ProductDao;
import entity.PageBean;
import entity.Product;
import java.sql.Connection;
import java.util.List;
import util.DbHelper;

/** 产品业务层：处理分页、表单校验、事务保存和逻辑下架。 */
public class ProductService
{
    /** 产品 DAO。 */
    private final ProductDao dao = new ProductDao();

    /** 查询产品分页，页码越界时自动修正。 */
    public PageBean<Product> page(int p, int size, String keyword)
    {
        p = Math.max(1, p);
        size = size <= 0 ? 10 : size;
        int total = dao.count(keyword);
        int pages = (int) Math.ceil((double) total / size);
        if (pages > 0 && p > pages)
        {
            p = pages;
        }
        return new PageBean<>(p, size, total, dao.page((p - 1) * size, size, keyword));
    }

    /** 查询所有上架产品，供产品明细下拉框使用。 */
    public List<Product> all()
    {
        return dao.all();
    }

    /** 查询一个产品；不存在时 DAO 返回 null。 */
    public Product get(int id)
    {
        return dao.findById(id);
    }

    /** 新增或编辑产品，id 是否为空决定调用哪个 DAO 方法。 */
    public boolean save(Product x)
    {
        // 产品名称是最基本的业务必填项。
        if (x == null || x.getProductName() == null || x.getProductName().isBlank())
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
            // 事务步骤 3：使用同一连接执行新增或修改。
            int n = x.getId() == null ? dao.save(conn, x) : dao.update(conn, x);
            // 事务步骤 4：提交。
            conn.commit();
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

    /** 逻辑下架产品。历史明细中的产品名称快照不会消失。 */
    public boolean delete(int id)
    {
        Connection conn = null;
        try
        {
            // 事务步骤 1、2。
            conn = DbHelper.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // 事务步骤 3：status 更新为 0。
            int n = dao.updateStatus(conn, id, 0);
            // 事务步骤 4。
            conn.commit();
            return n > 0;
        }
        catch (Exception e)
        {
            // 事务步骤 5。
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
            // 事务步骤 6。
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
