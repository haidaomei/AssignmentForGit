package servlet;

import entity.Customer;
import entity.PageBean;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import service.CommonService;
import service.CustomerService;

/**
 * 客户管理 Servlet，统一处理 {@code /customer/*} 下的列表、新增、修改、详情、删除和转移路由。
 *
 * <p>
 * 使用一个 Servlet 配合 {@code pathInfo} 分派多个相关动作，能使模块的入口集中，同时保持 URL 语义清晰。
 */
@WebServlet("/customer/*")
public class CustomerServlet extends BaseServlet
{
    /**
     * 客户业务层，处理查询、保存、逻辑删除和转移。
     */
    private final CustomerService service = new CustomerService();

    /**
     * 通用下拉选项业务，提供等级、来源和负责人。
     */
    private final CommonService common = new CommonService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        try
        {
            if (p == null || "/list".equals(p) || "/search".equals(p))
            {
                list(req, resp);
            }
            else if ("/add".equals(p))
            {
                form(req, resp, null);
            }
            else if ("/edit".equals(p))
            {
                form(req, resp, service.get(intVal(req.getParameter("id"), 0), user(req)));
            }
            else if ("/detail".equals(p))
            {
                detail(req, resp);
            }
            else
            {
                list(req, resp);
            }
        }
        catch (Exception e)
        {
            req.setAttribute("errorMessage", e.getMessage());
            forward(req, resp, "/error.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        boolean ok = false;
        if ("/save".equals(p))
        {
            Customer x = read(req);
            ok = x.getId() == null ? service.add(x) : service.update(x);
            flash(req, ok, "客户保存成功");
        }
        else if ("/delete".equals(p))
        {
            ok = service.delete(intVal(req.getParameter("id"), 0));
            flash(req, ok, "客户已删除");
        }
        else if ("/transfer".equals(p))
        {
            ok = service.transfer(intVal(req.getParameter("id"), 0), intVal(req.getParameter("toUserId"), 0), req.getParameter("reason"));
            flash(req, ok, "客户转移成功");
        }
        redirect(req, resp, "/customer/list");
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // 先统一去除关键词首尾空格，再把同一个值同时用于数据库查询和搜索框回显。
        String keyword = keyword(req);
        PageBean<Customer> pb = service.page(intVal(req.getParameter("currentPage"), 1), 10, keyword, user(req));
        req.setAttribute("pageBean", pb);
        req.setAttribute("customerList", pb.getData());
        req.setAttribute("keyword", keyword);
        req.setAttribute("activeMenu", "customer");
        forward(req, resp, "/customer_list.jsp");
    }

    private void form(HttpServletRequest req, HttpServletResponse resp, Customer x) throws ServletException, IOException
    {
        req.setAttribute("customer", x);
        req.setAttribute("levelList", common.levels());
        req.setAttribute("sourceList", common.sources());
        req.setAttribute("userList", common.users());
        req.setAttribute("activeMenu", "customer");
        forward(req, resp, "/customer_form.jsp");
    }

    private void detail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        int id = intVal(req.getParameter("id"), 0);
        Customer x = service.get(id, user(req));
        if (x == null)
        {
            resp.sendError(404);
            return;
        }
        req.setAttribute("customer", x);
        req.setAttribute("contactList", service.contacts(id));
        req.setAttribute("opportunityList", service.opportunities(id));
        req.setAttribute("followList", service.follows(id));
        req.setAttribute("transferList", service.transfers(id));
        req.setAttribute("userList", common.users());
        req.setAttribute("activeMenu", "customer");
        forward(req, resp, "/customer_detail.jsp");
    }

    private Customer read(HttpServletRequest r)
    {
        Customer x = new Customer();
        x.setId(integer(r.getParameter("id")));
        x.setCustomerName(r.getParameter("customerName"));
        x.setIndustry(r.getParameter("industry"));
        x.setScale(r.getParameter("scale"));
        x.setProvince(r.getParameter("province"));
        x.setCity(r.getParameter("city"));
        x.setAddress(r.getParameter("address"));
        x.setWebsite(r.getParameter("website"));
        x.setLevelId(integer(r.getParameter("levelId")));
        x.setSourceId(integer(r.getParameter("sourceId")));
        x.setOwnerUserId(integer(r.getParameter("ownerUserId")));
        if (x.getOwnerUserId() == null)
        {
            x.setOwnerUserId(user(r).getId());
        }
        x.setCreditRating(r.getParameter("creditRating"));
        x.setDescription(r.getParameter("description"));
        return x;
    }
}
/**
 * GET 根据 pathInfo 分派到列表、表单或详情页。
 */
// 例如访问 /customer/edit 时，p 的值是 /edit。
// 无子路径、/list 和 /search 都进入同一个分页查询。
// 新增表单传 null；编辑表单先按 id 和当前用户权限查出数据。
// 将意外异常转发到统一错误页，避免把服务器堆栈显示给用户。
/** POST 处理保存、逻辑删除和客户转移等会修改数据的动作。 */
// ok 统一表示业务是否成功，用于生成成功/失败提示。
// 把表单封装为实体；id 为 null 时新增，否则修改。
// 删除实际上是把 status 置 0，不会执行 SQL DELETE。
// 转移同时提交客户编号、新负责人和原因。
// 采用 PRG（Post/Redirect/Get）模式，避免用户刷新后重复保存。
/** 准备客户分页列表所需的 request 属性。 */
// 页码非法时默认第 1 页，每页固定 10 条。
// 同时放入完整 PageBean 和当页数据，方便 JSP 分别绘制分页条与表格。
/** 准备新增/编辑表单及其下拉选项。 */
// x 为 null 表示新增，非 null 时 JSP 会把原值回显到表单。
/** 准备客户详情和其联系人、商机、跟进、转移历史。 */
// 记录不存在或当前用户无权查看时，返回 HTTP 404 并立即结束。
// 关联列表分别放入 request，详情 JSP 可在一页中展示完整客户视图。
/** 把客户表单字段封装为 Customer JavaBean。 */
// integer 允许空值，因此新增表单没有 id 时不会报错。
// 没有显式选择负责人时，默认为当前登录用户。
