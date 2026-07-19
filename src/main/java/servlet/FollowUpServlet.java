package servlet;

import entity.Customer;
import entity.FollowUp;
import entity.PageBean;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.*;

/** 跟进记录 Servlet，处理条件分页、新增跟进、下次提醒和逻辑删除。 */
@WebServlet("/follow/*")
public class FollowUpServlet extends BaseServlet
{
    // 主 Service 用于跟进业务，其余 Service 为表单联动和数据归属提供数据。
    private final FollowUpService service = new FollowUpService();
    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        if (p == null || "/list".equals(p) || "/search".equals(p))
        {
            // 列表不再解析方式和日期筛选，只把去除首尾空格后的关键词交给 Service。
            String keyword = keyword(req);
            PageBean<FollowUp> pb = service.page(intVal(req.getParameter("currentPage"), 1), 10, keyword, user(req));
            req.setAttribute("pageBean", pb);
            req.setAttribute("followList", pb.getData());
            // 回显普通 GET 搜索的关键词，不在浏览器中发起实时请求。
            req.setAttribute("keyword", keyword);
            req.setAttribute("activeMenu", "follow");
            forward(req, resp, "/follow_list.jsp");
            return;
        }
        req.setAttribute("customerList", customerService.all(user(req)));
        req.setAttribute("selectedCustomerId", intVal(req.getParameter("customerId"), 0));
        req.setAttribute("selectedOpportunityId", intVal(req.getParameter("opportunityId"), 0));
        req.setAttribute("activeMenu", "follow");
        forward(req, resp, "/follow_form.jsp");
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
            FollowUp x = new FollowUp();
            x.setCustomerId(integer(req.getParameter("customerId")));
            x.setOpportunityId(integer(req.getParameter("opportunityId")));
            x.setContactId(integer(req.getParameter("contactId")));
            x.setFollowType(req.getParameter("followType"));
            x.setFollowContent(req.getParameter("followContent"));
            x.setCustomerFeedback(req.getParameter("customerFeedback"));
            x.setNextPlan(req.getParameter("nextPlan"));
            x.setNextFollowTime(normalize(req.getParameter("nextFollowTime")));
            x.setFollowUserId(user(req).getId());
            x.setFollowTime(normalize(req.getParameter("followTime")));
            if (x.getFollowTime() == null)
                x.setFollowTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            ok = allowed(req, x.getCustomerId()) && service.add(x);
        }
        flash(req, ok, "跟进记录保存成功");
        redirect(req, resp, "/follow/list");
    }

    private String normalize(String x)
    {
        return x == null || x.isBlank() ? null : x.replace('T', ' ') + (x.length() == 16 ? ":00" : "");
    }

    private boolean allowed(HttpServletRequest req, Integer cid)
    {
        if (cid == null)
            return false;
        if (!user(req).isSales())
            return true;
        for (Customer c : customerService.all(user(req)))
            if (c.getId().equals(cid))
                return true;
        return false;
    }
}
/** GET 处理分页列表或新增表单。 */
// 列表支持跟进类型、开始日期和结束日期筛选。
// 表单页加载当前用户可见客户，并支持通过 URL 预选客户/商机。
/** POST 处理逻辑删除或保存一条新跟进。 */
// /delete 只需记录主键；其他路径将表单封装为 FollowUp。
// 客户必填，商机和联系人是可选关联。
// HTML datetime-local 使用 T 分隔日期时间，normalize 会转成 MySQL 可接受格式。
// 跟进人不从前端接收，而是强制使用当前 Session 用户，防止伪造。
// 表单未填跟进时间时，默认为服务器当前时间。
// 归属校验通过后，Service 在同一事务中保存跟进并更新客户时间。
/**
 * 将 datetime-local 的 {@code yyyy-MM-dd'T'HH:mm} 转成 {@code yyyy-MM-dd HH:mm:ss}。
 */
// 空值返回 null；有值则将 T 换成空格，并在只精确到分时补上 :00 秒。
/** 校验销售员是否拥有要跟进的客户。 */
