package filter;

import entity.Customer;
import entity.User;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.*;

/**
 * 销售员写操作的数据归属过滤器。
 *
 * <p>
 * 例如销售员可能手工把 {@code id=10} 改成 {@code id=11}，试图修改其他人的数据。本过滤器会在 POST
 * 写操作前查询记录归属，只允许销售员修改自己负责的客户及关联业务。
 */
@WebFilter(urlPatterns =
{"/customer/*", "/contact/*", "/opportunity/*", "/follow/*", "/contract/*"})
public class OwnershipFilter implements Filter
{
    // 不同路径需要通过对应 Service 查询记录是否在当前用户的数据范围内。
    private final CustomerService customers = new CustomerService();
    private final ContactService contacts = new ContactService();
    private final OpportunityService opportunities = new OpportunityService();
    private final FollowUpService follows = new FollowUpService();
    private final ContractService contracts = new ContractService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        User user = (User) req.getSession().getAttribute("user");
        if (user == null || !user.isSales() || !"POST".equalsIgnoreCase(req.getMethod()))
        {
            chain.doFilter(request, response);
            return;
        }
        String path = req.getRequestURI().substring(req.getContextPath().length());
        int id = intVal(req.getParameter("id"));
        Integer customerId = integer(req.getParameter("customerId"));
        boolean allowed = true;
        if (path.startsWith("/customer/") && id > 0)
            allowed = customers.get(id, user) != null;
        else if (path.startsWith("/contact/") && id > 0)
            allowed = contacts.get(id, user) != null;
        else if (path.startsWith("/opportunity/") && id > 0)
            allowed = opportunities.get(id, user) != null;
        else if (path.startsWith("/follow/delete") && id > 0)
            allowed = follows.accessible(id, user);
        else if (path.startsWith("/contract/") && id > 0)
            allowed = contracts.get(id, user) != null;
        else if (customerId != null)
            allowed = owns(customerId, user);
        if (!allowed)
        {
            resp.setStatus(403);
            req.setAttribute("errorMessage", "不能修改不属于您的客户数据");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
            return;
        }
        chain.doFilter(request, response);
    }

    /** 遍历当前销售员的客户列表，判断是否拥有指定客户。 */
    private boolean owns(int customerId, User user)
    {
        for (Customer c : customers.all(user))
            if (c.getId() == customerId)
                return true;
        return false;
    }

    /** 把文本安全转成 int；空值或非数字时返回 0，避免请求直接抛异常。 */
    private int intVal(String v)
    {
        try
        {
            return v == null ? 0 : Integer.parseInt(v);
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    /** 把可选文本转成 Integer；没有合法值时返回 null。 */
    private Integer integer(String v)
    {
        try
        {
            return v == null || v.isBlank() ? null : Integer.valueOf(v);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
/** 对客户、联系人、商机、跟进和合同的 POST 请求执行归属检查。 */
// 只针对已登录的销售员 POST 写操作；管理员/经理和只读 GET 直接放行。
// 去掉上下文路径后，path 形如 /customer/update，便于判断模块。
// id 是当前记录主键；customerId 是新增关联数据时所属的客户主键。
// 默认允许，当路径与主键满足某个分支时，再执行对应的归属查询。
// 新增数据常常没有 id，此时改为检查 customerId 是否属于当前销售员。
// 未通过时返回 403 并终止请求，不会进入 Servlet 执行更新。
// 通过归属校验后继续请求链。
