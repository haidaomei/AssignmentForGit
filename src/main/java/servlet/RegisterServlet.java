package servlet;

import entity.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.LoginService;

/** 销售员自助注册 Servlet，新账号的角色由业务层固定为销售员。 */
@WebServlet("/register")
public class RegisterServlet extends BaseServlet
{
    /** 登录/注册业务层。 */
    private final LoginService service = new LoginService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        forward(req, resp, "/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String expected = (String) req.getSession().getAttribute("checkCode");
        String code = req.getParameter("code"), password = req.getParameter("password");
        if (expected == null || code == null || !expected.equalsIgnoreCase(code.trim()))
        {
            req.setAttribute("error", "验证码不正确");
            forward(req, resp, "/register.jsp");
            return;
        }
        if (password == null || !password.equals(req.getParameter("confirmPassword")))
        {
            req.setAttribute("error", "两次输入的密码不一致");
            forward(req, resp, "/register.jsp");
            return;
        }
        User u = new User();
        u.setUsername(req.getParameter("username"));
        u.setPassword(password);
        u.setRealName(req.getParameter("realName"));
        u.setPhone(req.getParameter("phone"));
        u.setEmail(req.getParameter("email"));
        if (service.register(u))
        {
            req.getSession().setAttribute("flashMsg", "注册成功，请使用新账号登录");
            req.getSession().setAttribute("flashType", "success");
            redirect(req, resp, "/login.jsp");
        }
        else
        {
            req.setAttribute("error", "注册失败，用户名可能已存在或信息不完整");
            forward(req, resp, "/register.jsp");
        }
    }
}
/** GET 请求打开注册页。 */
/** POST 请求校验表单并创建用户。 */
// 先校验验证码，防止自动化批量注册。
// 两次密码必须完全相同，否则不继续调用数据库。
// 将 HTTP 表单中的字符串逐项封装到 User 实体对象。
// 注册成功时把一次性消息放入 Session，重定向后的登录页仍可显示它。
// 失败时使用 request 转发，当前页面可立即显示错误原因。
