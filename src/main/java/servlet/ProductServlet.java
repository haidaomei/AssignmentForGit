package servlet;

import entity.PageBean;
import entity.Product;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.ProductService;

/** 产品管理 Servlet，处理分页搜索、新增、修改和逻辑删除。 */
@WebServlet("/product/*")
public class ProductServlet extends BaseServlet
{
    /** 产品业务层。本 Servlet 还会被 AuthorizationFilter 限制为管理员/销售经理访问。 */
    private final ProductService service = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        if (p == null || "/list".equals(p) || "/search".equals(p))
        {
            // 规范化后的关键词用于参数化模糊查询，也会原样交给 JSP 显示。
            String keyword = keyword(req);
            PageBean<Product> pb = service.page(intVal(req.getParameter("currentPage"), 1), 10, keyword);
            req.setAttribute("pageBean", pb);
            req.setAttribute("productList", pb.getData());
            req.setAttribute("keyword", keyword);
            req.setAttribute("activeMenu", "product");
            forward(req, resp, "/product_list.jsp");
            return;
        }
        req.setAttribute("product", "/edit".equals(p) ? service.get(intVal(req.getParameter("id"), 0)) : null);
        req.setAttribute("activeMenu", "product");
        forward(req, resp, "/product_form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            utf8(req, resp);
        }
        catch (Exception ignored)
        {
        }
        boolean ok;
        if ("/delete".equals(req.getPathInfo()))
            ok = service.delete(intVal(req.getParameter("id"), 0));
        else
        {
            Product x = new Product();
            x.setId(integer(req.getParameter("id")));
            x.setProductName(req.getParameter("productName"));
            x.setCategory(req.getParameter("category"));
            x.setUnit(req.getParameter("unit"));
            x.setUnitPrice(decimal(req.getParameter("unitPrice")));
            x.setDescription(req.getParameter("description"));
            ok = service.save(x);
        }
        flash(req, ok, "产品保存成功");
        redirect(req, resp, "/product/list");
    }
}
/** GET 处理列表、搜索和表单页。 */
// 列表与搜索共用同一分页查询，每页 10 条。
// 把分页对象、当页数据和关键词放入 request 供 JSP 使用。
// /edit 时查询原产品用于回显，/add 时传入 null 表示空表单。
/** POST 处理保存或逻辑删除。 */
// 根据 pathInfo 区分删除和保存。
// 将表单字段封装为 Product，decimal 用 BigDecimal 安全解析货币单价。
// 一次性提示存在 Session 中，重定向到列表后显示。
