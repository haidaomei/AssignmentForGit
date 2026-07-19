package servlet;

import com.google.gson.Gson;
import entity.Contact;
import entity.Customer;
import entity.PageBean;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.ContactService;
import service.CustomerService;

/**
 * 联系人管理 Servlet，同时提供按客户联动查询联系人的 JSON 接口。
 *
 * <p>
 * 商机表单选择客户后，可请求 {@code /contact/options?customerId=...} 动态刷新联系人下拉框。
 */
@WebServlet("/contact/*")
public class ContactServlet extends BaseServlet
{
    /** 联系人业务。 */
    private final ContactService service = new ContactService();

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        if ("/options".equals(p))
        {
            int customerId = intVal(req.getParameter("customerId"), 0);
            if (!allowed(req, customerId))
            {
                resp.sendError(403);
                return;
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(new Gson().toJson(service.byCustomer(customerId)));
            return;
        }
        if (p == null || "/list".equals(p) || "/search".equals(p))
        {
            // 普通 GET 搜索只在提交表单后执行；去掉两端空格可避免无意义的查询差异。
            String keyword = keyword(req);
            PageBean<Contact> pb = service.page(intVal(req.getParameter("currentPage"), 1), 10, keyword, user(req));
            req.setAttribute("pageBean", pb);
            req.setAttribute("contactList", pb.getData());
            req.setAttribute("keyword", keyword);
            req.setAttribute("activeMenu", "contact");
            forward(req, resp, "/contact_list.jsp");
            return;
        }
        Contact x = "/edit".equals(p) ? service.get(intVal(req.getParameter("id"), 0), user(req)) : null;
        req.setAttribute("contact", x);
        req.setAttribute("customerList", customerService.all(user(req)));
        req.setAttribute("selectedCustomerId", intVal(req.getParameter("customerId"), 0));
        req.setAttribute("activeMenu", "contact");
        forward(req, resp, "/contact_form.jsp");
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
        String p = req.getPathInfo();
        boolean ok;
        if ("/delete".equals(p))
            ok = service.delete(intVal(req.getParameter("id"), 0));
        else
        {
            Contact x = read(req);
            ok = allowed(req, x.getCustomerId()) && service.save(x);
        }
        flash(req, ok, "联系人保存成功");
        redirect(req, resp, "/contact/list");
    }

    private Contact read(HttpServletRequest r)
    {
        Contact x = new Contact();
        x.setId(integer(r.getParameter("id")));
        x.setCustomerId(integer(r.getParameter("customerId")));
        x.setName(r.getParameter("name"));
        x.setGender(r.getParameter("gender"));
        x.setPosition(r.getParameter("position"));
        x.setPhone(r.getParameter("phone"));
        x.setEmail(r.getParameter("email"));
        x.setWechat(r.getParameter("wechat"));
        x.setIsPrimary("1".equals(r.getParameter("isPrimary")) ? 1 : 0);
        x.setIsDecisionMaker("1".equals(r.getParameter("isDecisionMaker")) ? 1 : 0);
        x.setHobby(r.getParameter("hobby"));
        x.setRemarks(r.getParameter("remarks"));
        return x;
    }

    private boolean allowed(HttpServletRequest req, Integer customerId)
    {
        if (customerId == null)
            return false;
        if (!user(req).isSales())
            return true;
        List<Customer> list = customerService.all(user(req));
        for (Customer c : list)
            if (c.getId().equals(customerId))
                return true;
        return false;
    }
}
/** 客户业务，用于下拉列表和归属校验。 */
/** GET 处理 JSON 联动、分页列表和新增/编辑表单。 */
// /options 是纯 JSON 数据接口。
// 返回数据前必须确认销售员拥有该客户，防止通过参数查看别人的联系人。
// 声明 UTF-8 JSON 类型，并用 Gson 序列化联系人列表。
// 列表和搜索共用分页逻辑。
// 将分页、数据、原关键词和激活菜单放入 request。
// 其他 GET 路径进入表单；/edit 查原数据，/add 使用 null。
// selectedCustomerId 支持从客户详情页进入新增联系人时预选客户。
/** POST 保存或逻辑删除联系人。 */
// doPost 只声明 IOException，因此对 utf8 可能的 ServletException 做容错处理。
// 删除路径执行逻辑删除；其他路径按保存处理。
// 保存前再检查客户归属，避免销售员向别人的客户添加联系人。
/** 将联系人表单参数封装成 JavaBean。 */
// 复选框被勾选时表单值为 1，否则将字段设为 0。
/** 判断当前用户是否可以访问指定客户。 */
// 客户编号不能为空；管理员和经理拥有全局权限。
// 销售员需遍历自己的客户列表，找到匹配主键才返回 true。
