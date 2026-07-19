package service;

import dao.CustomerDao;
import dao.FollowUpDao;
import dao.OpportunityDao;
import entity.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import util.DbHelper;

/**
 * 商机主从表与阶段推进业务层。
 *
 * <p>
 * “主从表”指一条商机主记录可对应多条产品明细。保存时会校验产品、计算小计和总金额，再把主表与明细表放在
 * 同一事务中写入。阶段推进还会自动生成系统跟进记录并更新客户最后跟进时间。
 */
public class OpportunityService
{
    /**
     * 商机主表和产品明细 DAO。
     */
    private final OpportunityDao dao = new OpportunityDao();

    /**
     * 阶段推进时用于保存系统跟进记录。
     */
    private final FollowUpDao followDao = new FollowUpDao();

    /**
     * 阶段推进时用于同步客户最后跟进时间。
     */
    private final CustomerDao customerDao = new CustomerDao();

    /**
     * 按商机编号、标题或客户名称模糊分页查询，同时应用销售员数据范围。
     */
    public PageBean<Opportunity> page(int p, int size, String keyword, User u)
    {
        // 保证页码至少为 1，每页大小默认为 10。
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
     * 按主键查询商机及其产品明细；DAO 会同时检查数据权限。
     */
    public Opportunity get(int id, User u)
    {
        return dao.findById(id, u.getId(), u.isSales());
    }

    /**
     * @return 某个客户下的全部有效商机
     */
    public List<Opportunity> byCustomer(int id)
    {
        return dao.byCustomer(id);
    }

    /**
     * 新增或修改商机。
     *
     * <p>
     * 新增时生成商机编号；修改时先将原明细逻辑失效，再写入表单中的新明细。这种“逻辑替换”方式既保留历史数据， 又使当前明细与表单完全一致。
     */
    public boolean save(Opportunity x)
    {
        // 商机标题和至少一条不重复的产品明细是必填数据。
        if (x == null || x.getTitle() == null || x.getTitle().isBlank() || !validItems(x.getItems()))
        {
            return false;
        }
        // 进入事务前先用 BigDecimal 重新计算总金额，不信任浏览器传回的金额。
        x.setExpectedAmount(total(x.getItems()));
        Connection conn = null;
        try
        {
            // 1. 从 Druid 连接池取连接。
            conn = DbHelper.getDataSource().getConnection();
            // 2. 关闭自动提交。
            conn.setAutoCommit(false);
            int id;
            if (x.getId() == null)
            {
                // 3a. 没有主键表示新增：生成当日流水号，插入主表并取回自增主键。
                x.setOpportunityNo(dao.nextNumber(conn));
                id = dao.save(conn, x);
            }
            else
            {
                // 3b. 有主键表示修改：更新主表，再使原产品明细失效。
                id = x.getId();
                if (dao.update(conn, x) <= 0)
                {
                    throw new IllegalStateException("商机不存在");
                }
                dao.deactivateItems(conn, id);
            }
            // 3c. 逐条保存新的产品明细，每条都使用同一个 conn。
            for (LineItem item : x.getItems())
            {
                dao.saveItem(conn, id, item);
            }
            // 4. 主表和全部明细都成功后提交。
            conn.commit();
            return true;
        }
        catch (Exception e)
        {
            // 5. 任一条 SQL 失败都回滚，避免留下没有明细的“半成品”商机。
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
            // 6. 最后归还连接。
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
     * 推进商机阶段，并记录一条自动跟进。
     *
     * @param probability
     *            新阶段的成交概率，非丢单阶段只能向前推进
     * @param reason
     *            丢单原因；进入“丢单”阶段时必填
     */
    public boolean advance(
            int id, int stageId, int probability, String stageName, String reason, User user)
    {
        // 先按权限查询原商机，同时校验概率不倒退、丢单时已填原因。
        Opportunity old = dao.findById(id, user.getId(), user.isSales());
        if (old == null || (!"丢单".equals(stageName) && probability <= old.getProbability()) || ("丢单".equals(stageName) && (reason == null || reason.isBlank())))
        {
            return false;
        }
        // 阶段名称决定业务状态：已成交、已丢单或仍在进行中。
        String state = "成交".equals(stageName) ? "已成交" : "丢单".equals(stageName) ? "已丢单" : "进行中";
        Connection conn = null;
        try
        {
            // 1、2. 获取连接并关闭自动提交。
            conn = DbHelper.getDataSource().getConnection();
            conn.setAutoCommit(false);
            // 3a. 更新阶段、概率、业务状态和丢单原因。
            dao.advance(conn, id, stageId, probability, state, reason);
            // 3b. 组装一条“系统记录”，使阶段变化可在跟进时间线中追溯。
            FollowUp f = new FollowUp();
            f.setCustomerId(old.getCustomerId());
            f.setOpportunityId(id);
            f.setContactId(old.getContactId());
            f.setFollowType("系统记录");
            f.setFollowContent("商机阶段由“" + old.getStageName() + "”推进到“" + stageName + "”" + (reason == null || reason.isBlank() ? "" : "，原因：" + reason));
            f.setFollowUserId(user.getId());
            // SimpleDateFormat 把当前 Java 时间转成 MySQL DATETIME 可识别的文本格式。
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            f.setFollowTime(now);
            followDao.save(conn, f);
            customerDao.updateLastFollowTime(conn, old.getCustomerId(), now);
            // 4. 三项写操作一起提交。
            conn.commit();
            return true;
        }
        catch (Exception e)
        {
            // 5. 任意一项失败则全部回滚。
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
            // 6. 归还数据库连接。
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
     * 逻辑删除商机；查询时会通过 status=1 隐藏它及其明细。
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
     * 重新计算每条明细的小计和整个商机的总金额。
     *
     * <p>
     * BigDecimal 适合货币计算，避免 double 二进制浮点数可能产生的小数误差。
     */
    private BigDecimal total(List<LineItem> items)
    {
        // 总金额从精确的 0 开始累加。
        BigDecimal t = BigDecimal.ZERO;
        for (LineItem i : items)
        {
            // 数量非法时默认为 1；单价为 null 时按 0 处理，防止空指针异常。
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
     * 校验明细非空、每行都选了产品，且同一产品没有重复出现。
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
}