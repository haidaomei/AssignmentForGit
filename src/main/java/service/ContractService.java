package service;

import dao.ContractDao;
import entity.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

import util.DbHelper;

/**
 * 合同主从表业务层。
 *
 * <p>
 * 一份合同由合同主表和多条产品明细组成。本类负责日期和明细校验、金额计算、合同编号生成以及主从表事务。
 */
public class ContractService
{
    /**
     * 合同主表与产品明细 DAO。
     */
    private final ContractDao dao = new ContractDao();

    /**
     * 按合同编号、名称或客户名称模糊分页查询，并按当前用户限制数据范围。
     */
    public PageBean<Contract> page(int p, int size, String keyword, User u)
    {
        // 页码不小于 1，非法每页数回退为 10。
        p = Math.max(1, p);
        size = size <= 0 ? 10 : size;
        int total = dao.count(keyword, u.getId(), u.isSales());
        int pages = (int) Math.ceil((double) total / size);
        if (pages > 0 && p > pages)
        {
            p = pages;
        }
        return new PageBean<>(p, size, total, dao.page((p - 1) * size, size, keyword, u.getId(), u.isSales()));
    }

    /**
     * 按主键查询合同及产品明细，DAO 会同时校验数据权限。
     */
    public Contract get(int id, User u)
    {
        return dao.findById(id, u.getId(), u.isSales());
    }

    /**
     * 判断一个商机是否已经生成有效合同，防止重复生成。
     */
    public boolean existsForOpportunity(int id)
    {
        return dao.existsForOpportunity(id);
    }

    /**
     * 新增或修改合同。
     *
     * <p>
     * 保存前会一次完成：合同名称校验、产品去重、商机重复生成检查、开始/结束日期顺序检查和服务端金额重算。
     */
    public boolean save(Contract x)
    {
        // 短路逻辑或 || 会在任意条件不合法时立即返回 false。
        if (x == null || x.getContractName() == null || x.getContractName().isBlank() || !validItems(x.getItems()) || (x.getId() == null && x.getOpportunityId() != null && dao.existsForOpportunity(x.getOpportunityId())) || (notBlank(x.getStartDate()) && notBlank(x.getEndDate()) && x.getStartDate().compareTo(x.getEndDate()) > 0))
        {
            return false;
        }
        // 服务端根据单价和数量计算合同总额，防止前端金额被篡改。
        x.setContractAmount(total(x.getItems()));
        Connection conn = null;
        try
        {
            // 1. 从连接池取得连接。
            conn = DbHelper.getDataSource().getConnection();
            // 2. 关闭自动提交。
            conn.setAutoCommit(false);
            int id;
            if (x.getId() == null)
            {
                // 3a. 新增：生成当日合同流水号，保存主表并获取自增主键。
                x.setContractNo(dao.nextNumber(conn));
                id = dao.save(conn, x);
            }
            else
            {
                // 3b. 修改：更新主表，将原明细逻辑失效，准备写入新明细。
                id = x.getId();
                if (dao.update(conn, x) <= 0)
                {
                    throw new IllegalStateException("合同不存在");
                }
                dao.deactivateItems(conn, id);
            }
            // 3c. 使用同一连接保存每条产品明细。
            for (LineItem item : x.getItems())
            {
                dao.saveItem(conn, id, item);
            }
            // 4. 主表和所有明细均成功后提交。
            conn.commit();
            return true;
        }
        catch (Exception e)
        {
            // 5. 任一步异常都回滚，避免主从数据不完整。
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
            // 6. 关闭连接（对连接池而言就是把连接归还池中）。
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
     * 逻辑删除合同，保留合同和明细的历史记录。
     */
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

    /**
     * 用 BigDecimal 精确计算合同明细小计与总额。
     */
    private BigDecimal total(List<LineItem> items)
    {
        // 从精确值 0 开始累加。
        BigDecimal t = BigDecimal.ZERO;
        for (LineItem i : items)
        {
            if (i.getQuantity() == null || i.getQuantity() <= 0)
            {
                i.setQuantity(1);
            }
            if (i.getUnitPrice() == null)
            {
                i.setUnitPrice(BigDecimal.ZERO);
            }
            i.setSubtotal(i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
            t = t.add(i.getSubtotal());
        }
        return t;
    }

    /**
     * 检查明细列表非空、产品编号非空，且同一产品不重复。
     */
    private boolean validItems(List<LineItem> items)
    {
        if (items == null || items.isEmpty())
        {
            return false;
        }
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (LineItem i : items)
        {
            if (i.getProductId() == null || !ids.add(i.getProductId()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return 字符串不为 null 且不是空白内容时返回 true
     */
    private boolean notBlank(String value)
    {
        return value != null && !value.isBlank();
    }
}