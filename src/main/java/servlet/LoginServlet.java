package servlet;

import entity.User;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.LoginService;

/**
 * 用户登录 Servlet。
 *
 * <p>
 * GET 用于打开页面，POST 用于接收表单、校验验证码和账号，登录成功后把 User 对象放入 Session。
 */
@WebServlet("/login")
public class LoginServlet extends BaseServlet
{
    /** 登录业务层，负责用户名和密码查询。 */
    private final LoginService service = new LoginService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        redirect(req, resp, "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String code = req.getParameter("code");
        String expected = (String) req.getSession().getAttribute("checkCode");
        if (expected == null || code == null || !expected.equalsIgnoreCase(code.trim()))
        {
            req.setAttribute("error", "验证码不正确");
            forward(req, resp, "/login.jsp");
            return;
        }
        String username = req.getParameter("username"), password = req.getParameter("password");
        User u = service.login(username, password);
        if (u == null)
        {
            req.setAttribute("error", "用户名或密码错误");
            forward(req, resp, "/login.jsp");
            return;
        }
        req.getSession().setAttribute("user", u);
        if ("1".equals(req.getParameter("remember")))
        {
            Cookie c = new Cookie("username", URLEncoder.encode(username, StandardCharsets.UTF_8));
            c.setMaxAge(7 * 24 * 3600);
            c.setHttpOnly(true);
            c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
            resp.addCookie(c);
        }
        else
        {
            Cookie c = new Cookie("username", "");
            c.setMaxAge(0);
            c.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
            resp.addCookie(c);
        }
        redirect(req, resp, "/dashboard");
    }
}
/** 访问 /login 时跳转到登录 JSP。 */
/** 处理登录表单提交。 */
// 统一中文请求/响应编码。
// 取出用户输入和 Session 中的正确验证码，忽略大小写比较。
// forward 保留 request 属性，因此页面可以显示 error。
// 调用 Service 查询未被删除的账号；失败时统一提示，不暴露是用户名还是密码错误。
// Session 会在后续请求中保留登录用户，Filter 也依靠这个属性判断是否登录。
// 勾选“记住登录”后，用 Cookie 记住编码后的用户名 7 天，不保存密码。
// HttpOnly 禁止页面 JavaScript 读取该 Cookie，减少 XSS 窃取风险。
// Path 限制 Cookie 在当前 Web 项目中生效。
// 未勾选时发送同名空 Cookie，MaxAge=0 要求浏览器立即删除旧 Cookie。
// 使用重定向进入仪表盘，避免刷新页面时重复提交登录表单。
