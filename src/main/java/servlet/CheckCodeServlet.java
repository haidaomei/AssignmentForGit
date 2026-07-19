package servlet;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import util.CheckCodeUtil;

/**
 * 图形验证码 Servlet，为登录和注册页输出 JPEG 图片。
 *
 * <p>
 * {@link WebServlet} 把这个 Java 类映射到 {@code /checkCodeServlet}，浏览器的 {@code <img>}
 * 标签请求该路径后，响应内容就是验证码图片本身。
 */
@WebServlet("/checkCodeServlet")
public class CheckCodeServlet extends HttpServlet
{
    /** GET 请求用于生成一张新验证码。 */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        // 告诉浏览器响应不是 HTML，而是 JPEG 图片二进制数据。
        resp.setContentType("image/jpeg");
        // 禁止缓存，否则点击刷新验证码时可能仍显示旧图片。
        resp.setHeader("Cache-Control", "no-store,no-cache,must-revalidate");
        String code = CheckCodeUtil.outputVerifyImage(112, 38, resp.getOutputStream(), 4);
        req.getSession().setAttribute("checkCode", code);
    }
}
// 生成 112×38 像素、4 位字符的图片，并直接写入响应输出流。
// 把正确答案保存到当前会话，登录/注册 POST 时再进行比对。
