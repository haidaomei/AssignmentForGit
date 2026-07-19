package servlet;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** 退出登录 Servlet，销毁当前 Session 后返回登录页。 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet
{
    /** 点击“退出”链接时执行。 */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        req.getSession().invalidate();
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }
}
// invalidate 使 Session 中的 user 和其他会话属性全部失效。
// 重定向中加上 contextPath，保证项目不是部署在根路径时也能正常跳转。
