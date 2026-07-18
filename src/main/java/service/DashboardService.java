package service;

import dao.DashboardDao;
import entity.DashboardStats;
import entity.FunnelData;
import entity.User;
import java.util.List;

/**
 * 仪表盘和销售漏斗的只读业务层。
 *
 * <p>
 * 这里没有写操作，所以不需要手动开启事务。当前用户的编号和是否为销售员会传给 DAO，DAO 据此决定查询全部数据，还是只查询该销售员负责的数据。
 */
public class DashboardService
{
    /** 仪表盘 DAO，封装各类统计 SQL。 */
    private final DashboardDao dao = new DashboardDao();

    /**
     * 查询仪表盘顶部卡片、最近跟进、今日提醒等统计。
     *
     * @param u
     *            当前登录用户，不能为 null
     * @return 聚合好的仪表盘数据对象
     */
    public DashboardStats stats(User u)
    {
        return dao.stats(u.getId(), u.isSales());
    }

    /**
     * 查询每个商机阶段的数量和金额，供 ECharts 绘制漏斗图。
     *
     * @param u
     *            当前登录用户
     * @return 按阶段聚合的漏斗数据
     */
    public List<FunnelData> funnel(User u)
    {
        return dao.funnel(u.getId(), u.isSales());
    }
}
