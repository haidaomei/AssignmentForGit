package filter;

import java.io.IOException;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录状态过滤器，防止未登录用户直接访问 CRM 业务页面。
 *
 * <p>
 * Filter 会在 Servlet 之前执行。本类放行登录、注册、验证码和静态资源，其余请求必须能在 Session 中找到 {@code user}。
 */
@WebFilter("/*")
public class LoginFilter implements Filter
{
    /** 检查当前请求是否属于白名单或已经登录。 */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        // 在全局入口统一编码，保证各模块中文参数正常。
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // 去掉 /crm-pro 这类上下文，得到项目内部路径。
        String uri = req.getRequestURI().substring(req.getContextPath().length());
        // /login.jsp 只是服务器内部视图；用户直接请求它时转到 /login，才能正确读取 Cookie。
        if ("/login.jsp".equals(uri))
        {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        // startsWith 使 /static/style.css 等子路径也能命中白名单。
        boolean open = Stream.of("/login", "/register.jsp", "/register", "/checkCodeServlet", "/static/", "/error.jsp").anyMatch(uri::startsWith);
        // 白名单请求或 Session 中已有登录用户，就把请求交给下一个 Filter/Servlet。
        if (open || req.getSession().getAttribute("user") != null)
        {
            chain.doFilter(request, response);
            return;
        }
        // 未登录请求统一重定向 /login，不再生成登录页红色提醒框。
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
