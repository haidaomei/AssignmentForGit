package servlet;

import entity.User;
import java.io.IOException;
import java.math.BigDecimal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 所有业务 Servlet 的公共父类。
 *
 * <p>
 * 继承 HttpServlet 后，本类把字符编码、参数转换、登录用户读取和页面跳转等重复代码集中起来。 子类通过
 * {@code extends BaseServlet} 直接使用这些
 * protected 方法。
 */
public abstract class BaseServlet extends HttpServlet
{
    /** 统一请求和响应为 UTF-8，防止中文表单参数或页面输出出现乱码。 */
    protected void utf8(HttpServletRequest req, HttpServletResponse resp) throws java.io.UnsupportedEncodingException
    {
        // 必须在第一次读取 request 参数之前设置请求编码。
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        // contentType 同时告诉浏览器这是 HTML，并明确采用 UTF-8 解码。
        resp.setContentType("text/html;charset=UTF-8");
    }

    /** 把字符串安全转成基本类型 int；为空或格式错误时返回调用者给定的默认值。 */
    protected int intVal(String value, int fallback)
    {
        try
        {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    /** 把字符串安全转成 Integer；返回 null 可表达“表单没有选择这一项”。 */
    protected Integer integer(String value)
    {
        try
        {
            return value == null || value.isBlank() ? null : Integer.valueOf(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    /** 把金额参数转换为精确的 BigDecimal，非法金额按 0 处理。 */
    protected BigDecimal decimal(String value)
    {
        try
        {
            return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value);
        }
        catch (NumberFormatException e)
        {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 读取并规范化列表页的模糊搜索关键词。
     *
     * <p>
     * trim 会删除用户无意输入的首尾空格；如果输入框是空白，返回空字符串，DAO 的参数化 LIKE
     * 查询就会按完整列表处理。所有列表共用此方法，可确保搜索和分页回显采用同一个值。
     */
    protected String keyword(HttpServletRequest req)
    {
        String value = req.getParameter("keyword");
        return value == null ? "" : value.trim();
    }

    /** 从 Session 中取得登录时保存的用户对象。登录过滤器保证业务请求中它不为 null。 */
    protected User user(HttpServletRequest req)
    {
        return (User) req.getSession().getAttribute("user");
    }

    /** 保存一次性提示消息。消息放在 Session 是因为写操作完成后会 redirect，原 request 已经失效。 */
    protected void flash(HttpServletRequest req, boolean ok, String success)
    {
        req.getSession().setAttribute("flashType", ok ? "success" : "error");
        req.getSession().setAttribute("flashMsg", ok ? success : "操作失败，请检查输入或数据关联");
    }

    /** 服务端转发到 JSP：地址栏不变，当前 request 中的数据可以继续被 JSP 读取。 */
    protected void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException
    {
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    /** 客户端重定向到新的业务 URL，用于写操作后的 PRG（Post/Redirect/Get）模式。 */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException
    {
        // contextPath 可能是 /crm-pro；拼上它后项目部署到任意上下文都不会产生错误链接。
        resp.sendRedirect(req.getContextPath() + path);
    }
}
