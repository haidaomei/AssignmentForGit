package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import service.CustomerService;

/**
 * 客户流失预警 Servlet，展示超过 30 天未跟进的客户。
 */
@WebServlet("/warning/*")
public class WarningServlet extends BaseServlet
{
    /**
     * 客户业务层。
     */
    private final CustomerService service = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        req.setAttribute("customerList", service.warnings(user(req)));
        req.setAttribute("activeMenu", "warning");
        forward(req, resp, "/warning_customer.jsp");
    }
}
/**
 * 查询预警列表并转发到预警页。
 */
// Service 根据当前用户角色返回全局或个人负责的预警客户。
