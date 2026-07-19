package servlet;

import entity.User;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import service.LoginService;

/**
 * 用户登录 Servlet。
 *
 * <p>
 * GET 请求读取“记住我”Cookie 并打开登录页；POST 请求校验验证码和账号。登录成功后，当前用户保存到
 * Session，后续请求便可识别身份。如果用户勾选“记住我”，还会把用户名和密码按课程需求保存 7 天。
 */
@WebServlet("/login")
public class LoginServlet extends BaseServlet
{
    /**
     * 课件要求的 Cookie 存活时间：7 天，单位为秒。
     */
    private static final int REMEMBER_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 登录业务层，负责根据用户名和密码查询有效用户。
     */
    private final LoginService service = new LoginService();

    /**
     * 打开登录页。
     *
     * <p>
     * 这里先从浏览器携带的 Cookie 中取出已记住的账号和密码，再作为 request 属性交给 JSP 回填。
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // 统一中文编码，防止 Cookie 解码后的中文用户名显示乱码。
        utf8(req, resp);
        // 分别读取用户名和密码 Cookie；不存在时返回 null。
        String rememberedUsername = readCookie(req, "username");
        String rememberedPassword = readCookie(req, "password");
        // JSP 通过这两个属性给输入框设置默认值。
        req.setAttribute("rememberedUsername", rememberedUsername);
        req.setAttribute("rememberedPassword", rememberedPassword);
        // 只有两个 Cookie 都存在时才将“记住我”复选框回显为勾选。
        req.setAttribute("remembered", rememberedUsername != null && rememberedPassword != null);
        // forward 是服务器内部转发，浏览器仍保持 /login 这个统一入口。
        forward(req, resp, "/login.jsp");
    }

    /**
     * 处理登录表单提交。
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // 统一请求和响应编码。
        utf8(req, resp);
        // 读取表单账号、密码和用户输入的验证码。
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String code = req.getParameter("code");
        // 正确验证码是生成图片时由 CheckCodeServlet 存入 Session 的。
        String expected = (String) req.getSession().getAttribute("checkCode");
        if (expected == null || code == null || !expected.equalsIgnoreCase(code.trim()))
        {
            // 页面按需求不再显示红色提醒框，但保留用户已输入的账号和密码。
            prepareFailedForm(req, username, password);
            forward(req, resp, "/login.jsp");
            return;
        }
        // 调用 Service 查询未被逻辑删除的用户。
        User user = service.login(username, password);
        if (user == null)
        {
            // 账号或密码不正确时留在登录页，不建立登录 Session。
            prepareFailedForm(req, username, password);
            forward(req, resp, "/login.jsp");
            return;
        }
        // 登录成功后把完整 User 对象保存到 Session，LoginFilter 会据此放行业务请求。
        req.getSession().setAttribute("user", user);
        if ("1".equals(req.getParameter("remember")))
        {
            // 只有用户勾选“记住我”且账号验证成功后，才写入两个 7 天 Cookie。
            writeCookie(req, resp, "username", username, REMEMBER_SECONDS);
            writeCookie(req, resp, "password", password, REMEMBER_SECONDS);
        }
        else
        {
            // 未勾选时用 MaxAge=0 删除之前可能已经存在的两个 Cookie。
            writeCookie(req, resp, "username", "", 0);
            writeCookie(req, resp, "password", "", 0);
        }
        // 使用 PRG 模式进入仪表盘，刷新时不会重复提交登录表单。
        redirect(req, resp, "/dashboard");
    }

    /**
     * 登录失败时准备输入框回显数据。
     */
    private void prepareFailedForm(HttpServletRequest req, String username, String password)
    {
        req.setAttribute("rememberedUsername", username);
        req.setAttribute("rememberedPassword", password);
        req.setAttribute("remembered", "1".equals(req.getParameter("remember")));
    }

    /**
     * 创建、更新或删除一个“记住我”Cookie。
     *
     * @param maxAge
     *            正数表示保存秒数，0 表示要求浏览器立即删除
     */
    private void writeCookie(
            HttpServletRequest req, HttpServletResponse resp, String name, String value, int maxAge)
    {
        // Cookie 对特殊字符有限制，先使用 UTF-8 URL 编码再存储。
        Cookie cookie = new Cookie(name, URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8));
        cookie.setMaxAge(maxAge);
        // HttpOnly 阻止页面 JavaScript 直接读取账号 Cookie。
        cookie.setHttpOnly(true);
        // 删除 Cookie 时名称和 path 必须与写入时一致。
        cookie.setPath(cookiePath(req));
        resp.addCookie(cookie);
    }

    /**
     * 遍历浏览器携带的 Cookie，找到指定名称后用 UTF-8 解码。
     */
    private String readCookie(HttpServletRequest req, String name)
    {
        Cookie[] cookies = req.getCookies();
        if (cookies == null)
        {
            return null;
        }
        for (Cookie cookie : cookies)
        {
            if (name.equals(cookie.getName()))
            {
                try
                {
                    // 正常情况下 Cookie 由本类写入，解码后即可恢复原始账号或密码。
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                }
                catch (IllegalArgumentException ignored)
                {
                    // 浏览器 Cookie 可被用户手工修改；遇到不完整的百分号编码时按“未记住”处理，
                    // 避免一个损坏的 Cookie 让整个登录页面返回 500 错误。
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 项目部署在根路径时使用 / ，否则使用当前 Tomcat 上下文路径。
     */
    private String cookiePath(HttpServletRequest req)
    {
        return req.getContextPath().isEmpty() ? "/" : req.getContextPath();
    }
}
