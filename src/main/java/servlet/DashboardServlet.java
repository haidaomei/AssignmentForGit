package servlet;

import com.google.gson.Gson;
import entity.DashboardStats;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.DashboardService;
import service.FollowUpService;

/** 仪表盘 Servlet，一次准备统计卡片、最近跟进、今日提醒和漏斗图数据。 */
@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet
{
    /** 统计与漏斗业务。 */
    private final DashboardService service = new DashboardService();

    private final FollowUpService followService = new FollowUpService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        DashboardStats stats = service.stats(user(req));
        req.setAttribute("stats", stats);
        req.setAttribute("warningCount", stats.getWarningCount());
        req.setAttribute("latestFollows", followService.latest(user(req), 5));
        req.setAttribute("todayFollows", followService.today(user(req)));
        req.setAttribute("funnelJson", new Gson().toJson(service.funnel(user(req))));
        req.setAttribute("activeMenu", "dashboard");
        forward(req, resp, "/dashboard.jsp");
    }
}
