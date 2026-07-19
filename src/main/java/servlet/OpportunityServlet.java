package servlet;

import com.google.gson.Gson;
import entity.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.*;

/**
 * 商机管理 Servlet，包含主从表保存、客户联动 JSON、详情和阶段推进。
 *
 * <p>
 * 商机表单中的多条产品使用同名数组参数提交，这里将对应位置的产品、数量和单价重新组装为 LineItem 列表。
 */
@WebServlet("/opportunity/*")
public class OpportunityServlet extends BaseServlet
{
    // 分别提供商机、客户、产品、字典、跟进和合同业务能力。
    private final OpportunityService service = new OpportunityService();
    private final CustomerService customerService = new CustomerService();
    private final ProductService productService = new ProductService();
    private final CommonService common = new CommonService();
    private final FollowUpService followService = new FollowUpService();
    private final ContractService contractService = new ContractService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        if ("/options".equals(p))
        {
            int cid = intVal(req.getParameter("customerId"), 0);
            if (!allowed(req, cid))
            {
                resp.sendError(403);
                return;
            }
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(new Gson().toJson(service.byCustomer(cid)));
            return;
        }
        if (p == null || "/list".equals(p) || "/search".equals(p))
        {
            // 统一去掉首尾空格；旧阶段和状态参数不会在这里读取，因此不会影响结果。
            String keyword = keyword(req);
            PageBean<Opportunity> pb = service.page(intVal(req.getParameter("currentPage"), 1), 10, keyword, user(req));
            req.setAttribute("pageBean", pb);
            req.setAttribute("opportunityList", pb.getData());
            // 把原关键词交给 JSP 回显，用户翻页或查看结果时仍能看到当前搜索内容。
            req.setAttribute("keyword", keyword);
            req.setAttribute("activeMenu", "opportunity");
            forward(req, resp, "/opportunity_list.jsp");
            return;
        }
        if ("/detail".equals(p))
        {
            detail(req, resp);
            return;
        }
        Opportunity x = "/edit".equals(p) ? service.get(intVal(req.getParameter("id"), 0), user(req)) : null;
        formData(req, x);
        forward(req, resp, "/opportunity_form.jsp");
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
        {
            ok = service.delete(intVal(req.getParameter("id"), 0));
            flash(req, ok, "商机已删除");
            redirect(req, resp, "/opportunity/list");
            return;
        }
        if ("/advance".equals(p))
        {
            int stageId = intVal(req.getParameter("stageId"), 0);
            Lookup target = null;
            for (Lookup s : common.stages())
                if (s.getId() == stageId)
                    target = s;
            ok = target != null && service.advance(intVal(req.getParameter("id"), 0), stageId, target.getProbability(), target.getName(), req.getParameter("reason"), user(req));
            flash(req, ok, "商机阶段已推进");
            redirect(req, resp, "/opportunity/detail?id=" + intVal(req.getParameter("id"), 0));
            return;
        }
        Opportunity x = read(req);
        ok = allowed(req, x.getCustomerId()) && service.save(x);
        flash(req, ok, "商机保存成功");
        redirect(req, resp, "/opportunity/list");
    }

    private void detail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        int id = intVal(req.getParameter("id"), 0);
        Opportunity x = service.get(id, user(req));
        if (x == null)
        {
            resp.sendError(404);
            return;
        }
        req.setAttribute("opportunity", x);
        req.setAttribute("followList", followService.byOpportunity(id));
        req.setAttribute("stageList", common.stages());
        req.setAttribute("hasContract", contractService.existsForOpportunity(id));
        req.setAttribute("activeMenu", "opportunity");
        forward(req, resp, "/opportunity_detail.jsp");
    }

    private void formData(HttpServletRequest req, Opportunity x)
    {
        req.setAttribute("opportunity", x);
        req.setAttribute("selectedCustomerId", intVal(req.getParameter("customerId"), 0));
        req.setAttribute("customerList", customerService.all(user(req)));
        req.setAttribute("productList", productService.all());
        req.setAttribute("stageList", common.stages());
        req.setAttribute("userList", common.users());
        req.setAttribute("activeMenu", "opportunity");
    }

    private Opportunity read(HttpServletRequest r)
    {
        Opportunity x = new Opportunity();
        x.setId(integer(r.getParameter("id")));
        x.setTitle(r.getParameter("title"));
        x.setCustomerId(integer(r.getParameter("customerId")));
        x.setContactId(integer(r.getParameter("contactId")));
        x.setStageId(integer(r.getParameter("stageId")));
        x.setEstimatedCloseDate(r.getParameter("estimatedCloseDate"));
        x.setOwnerUserId(integer(r.getParameter("ownerUserId")));
        if (x.getOwnerUserId() == null)
            x.setOwnerUserId(user(r).getId());
        x.setDescription(r.getParameter("description"));
        x.setResultReason(r.getParameter("resultReason"));
        for (Lookup s : common.stages())
            if (s.getId().equals(x.getStageId()))
                x.setProbability(s.getProbability());
        x.setItems(readItems(r));
        return x;
    }

    private List<LineItem> readItems(HttpServletRequest r)
    {
        String[] products = r.getParameterValues("items[][productId]");
        String[] quantities = r.getParameterValues("items[][quantity]");
        String[] prices = r.getParameterValues("items[][unitPrice]");
        List<LineItem> list = new ArrayList<>();
        if (products == null)
            return list;
        for (int i = 0; i < products.length; i++)
        {
            Integer pid = integer(products[i]);
            if (pid == null)
                continue;
            LineItem x = new LineItem();
            x.setProductId(pid);
            x.setQuantity(intVal(quantities != null && i < quantities.length ? quantities[i] : null, 1));
            x.setUnitPrice(decimal(prices != null && i < prices.length ? prices[i] : null));
            list.add(x);
        }
        return list;
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
/** GET 处理联动 JSON、分页列表、详情与表单页。 */
// /options 按客户返回商机 JSON，供合同或跟进表单联动使用。
// 查询前校验客户归属，无权时返回 HTTP 403。
// 列表支持页码、阶段和业务状态筛选。
// 阶段字典同时供页面筛选下拉框使用。
// 详情页有独立的关联数据准备逻辑。
// 除列表/详情外的 GET 请求进入新增或编辑表单。
/** POST 处理逻辑删除、阶段推进和主从表保存。 */
// 逻辑删除完成后回到列表，return 防止继续进入其他分支。
// 阶段推进时不直接信任前端概率和名称，而是根据 stageId 在服务端字典中寻找。
// target 为 null 代表传入了不存在的阶段，利用 && 短路规则不调用业务层。
// 推进后返回原商机详情，用户可立即看到新阶段和系统跟进。
// 其他 POST 视为保存：先读取表单，再校验客户归属，最后调用事务 Service。
/** 查询商机详情所需的跟进、阶段和合同状态。 */
// 记录不存在或无权访问时返回 404。
// hasContract 用于决定是否显示“生成合同”按钮。
/** 统一准备商机新增/编辑表单的所有下拉选项。 */
// selectedCustomerId 使从客户详情页跳转过来时能预选客户。
/** 将商机主表字段和动态产品行封装成 Opportunity。 */
// 负责人未选择时，默认使用当前登录用户。
// 概率从服务端阶段字典获取，避免请求参数篡改概率。
// 动态产品行被单独解析为明细列表。
/**
 * 解析 {@code items[][productId]} / quantity / unitPrice 三组同位数组参数。
 *
 * <p>
 * 索引 i 相同的三个值属于同一行产品明细。
 */
// 表单没有任何产品参数时返回空列表，Service 会判定为校验失败。
// 空产品行直接跳过；continue 表示继续下一次循环。
// 数组过短或值非法时使用默认数量 1/空单价，Service 会再做规范化。
/** 管理员/经理可访问所有客户；销售员只能访问自己负责的客户。 */
