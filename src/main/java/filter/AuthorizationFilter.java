package filter;

import entity.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务端角色权限过滤器。
 *
 * <p>
 * 前端隐藏菜单只是为了用户体验，用户仍可手工输入 URL。因此后端必须再做一次鉴权：销售员不能进入产品维护 和客户转移接口。
 */
@WebFilter(urlPatterns =
{"/product/*", "/customer/transfer"})
public class AuthorizationFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    /** 检查 Session 中的用户角色，通过后才继续请求链。 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        User u = (User) req.getSession().getAttribute("user");
        // 理论上 LoginFilter 已经拦截未登录请求，这里再校验一次是防御性编程。
        if (u == null)
        {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        if (u.isSales())
        {
            resp.setStatus(403);
            req.setAttribute("errorMessage", "您没有权限访问此功能");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
            return;
        }
        chain.doFilter(request, response);
    }
}
// 从 Session 取出登录时保存的 User 对象。
// 销售员访问受限路径时返回 HTTP 403（Forbidden，服务器理解请求但拒绝执行）。
// 管理员或销售经理通过检查，继续进入目标 Servlet。
