package servlet;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.DashboardService;

/** 销售漏斗 Servlet，既可返回完整页面，也可作为 JSON 数据接口。 */
@WebServlet("/funnel")
public class FunnelServlet extends BaseServlet
{
    /** 提供按商机阶段聚合的数量与金额。 */
    private final DashboardService service = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String json = new Gson().toJson(service.funnel(user(req)));
        if ("json".equals(req.getParameter("format")))
        {
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(json);
            return;
        }
        req.setAttribute("funnelJson", json);
        req.setAttribute("activeMenu", "funnel");
        forward(req, resp, "/funnel_chart.jsp");
    }
}
