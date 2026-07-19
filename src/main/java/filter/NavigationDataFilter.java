package filter;

import entity.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import service.DashboardService;

/**
 * 全局导航数据过滤器。
 *
 * <p>
 * 顶部栏和侧边栏在很多页面中复用，因此用过滤器统一准备未跟进预警数，不需要每个 Servlet 都写一遍。
 */
@WebFilter("/*")
public class NavigationDataFilter implements Filter
{
    /** 通过仪表盘业务查询当前用户可见的预警数。 */
    private final DashboardService service = new DashboardService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        User u = (User) req.getSession().getAttribute("user");
        if (u != null)
        {
            try
            {
                req.setAttribute("warningCount", service.stats(u).getWarningCount());
            }
            catch (Exception ignored)
            {
            }
        }
        chain.doFilter(request, response);
    }
}
/** 登录用户访问页面时，向当前 request 放入 warningCount。 */
// 未登录的登录/注册页不需要查询预警。
// request 属性只在本次请求有效，JSP 可以通过 ${warningCount} 读取。
// 导航计数是辅助信息，查询失败不应阻止主页面打开。
// 无论是否查到计数，都要继续原请求。
