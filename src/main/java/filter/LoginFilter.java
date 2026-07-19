package filter;

import java.io.IOException;
import java.util.stream.Stream;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录状态过滤器，防止未登录用户直接访问 CRM 业务页面。
 *
 * <p>
 * Filter（过滤器）会在请求到达 Servlet 之前执行。这里使用 {@code /*} 拦截所有路径，然后对登录、注册、
 * 验证码和静态资源建立白名单。
 */
@WebFilter("/*")
public class LoginFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    /** 每个匹配请求都会进入此方法。 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        // ServletRequest/Response 是通用接口，转型后才能使用 Session、URI 和重定向等 HTTP 功能。
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // 统一请求和响应编码，防止中文表单内容出现乱码。
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // getRequestURI 包含项目上下文，substring 去掉它后得到项目内部路径。
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        // startsWith 使 /static/css/... 这类子路径也能命中白名单。
        boolean open = Stream.of("/login.jsp", "/login", "/register.jsp", "/register", "/checkCodeServlet", "/static/", "/error.jsp").anyMatch(uri::startsWith);
        // 白名单请求或 Session 中已有 user，就把控制权交给下一个 Filter/Servlet。
        if (open || req.getSession().getAttribute("user") != null)
        {
            chain.doFilter(request, response);
            return;
        }
        req.setAttribute("login_msg", "请先登录后继续");
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
}
// 未登录时使用服务器内部转发，从而能在登录页显示提示文字。
