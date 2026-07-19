package servlet;

import entity.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import service.*;

/**
 * 合同主从表管理 Servlet。
 *
 * <p>
 * 除常规列表、详情和编辑外，还支持从“已成交”商机生成合同，自动带入客户、商机名称和产品明细。
 */
@WebServlet("/contract/*")
public class ContractServlet extends BaseServlet
{
    // 合同 Service 处理主从表事务，其余 Service 为表单选项和商机预填提供数据。
    private final ContractService service = new ContractService();
    private final CustomerService customerService = new CustomerService();
    private final OpportunityService opportunityService = new OpportunityService();
    private final ProductService productService = new ProductService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        utf8(req, resp);
        String p = req.getPathInfo();
        if (p == null || "/list".equals(p) || "/search".equals(p))
        {
            // 合同列表只接收 keyword；旧状态参数即使出现在 URL 中也不会被读取。
            String keyword = keyword(req);
            PageBean<Contract> pb = service.page(intVal(req.getParameter("currentPage"), 1), 10, keyword, user(req));
            req.setAttribute("pageBean", pb);
            req.setAttribute("contractList", pb.getData());
            // 保留搜索框内的原关键词，并让分页链接能继续携带它。
            req.setAttribute("keyword", keyword);
            req.setAttribute("activeMenu", "contract");
            forward(req, resp, "/contract_list.jsp");
            return;
        }
        if ("/detail".equals(p))
        {
            Contract x = service.get(intVal(req.getParameter("id"), 0), user(req));
            if (x == null)
            {
                resp.sendError(404);
                return;
            }
            req.setAttribute("contract", x);
            req.setAttribute("activeMenu", "contract");
            forward(req, resp, "/contract_detail.jsp");
            return;
        }
        Contract x = "/edit".equals(p) ? service.get(intVal(req.getParameter("id"), 0), user(req)) : prefill(req);
        req.setAttribute("contract", x);
        req.setAttribute("customerList", customerService.all(user(req)));
        req.setAttribute("productList", productService.all());
        req.setAttribute("activeMenu", "contract");
        forward(req, resp, "/contract_form.jsp");
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
            Contract x = read(req);
            ok = allowed(req, x.getCustomerId()) && service.save(x);
        }
        flash(req, ok, "合同保存成功");
        redirect(req, resp, "/contract/list");
    }

    private Contract prefill(HttpServletRequest req)
    {
        Integer id = integer(req.getParameter("opportunityId"));
        if (id == null)
            return null;
        Opportunity o = opportunityService.get(id, user(req));
        if (o == null || !"已成交".equals(o.getBusinessStatus()) || service.existsForOpportunity(id))
            return null;
        Contract x = new Contract();
        x.setOpportunityId(id);
        x.setOpportunityTitle(o.getTitle());
        x.setContractName(o.getTitle() + "合同");
        x.setCustomerId(o.getCustomerId());
        x.setBusinessStatus("执行中");
        x.setItems(o.getItems());
        return x;
    }

    private Contract read(HttpServletRequest r)
    {
        Contract x = new Contract();
        x.setId(integer(r.getParameter("id")));
        x.setContractName(r.getParameter("contractName"));
        x.setOpportunityId(integer(r.getParameter("opportunityId")));
        x.setCustomerId(integer(r.getParameter("customerId")));
        x.setSignedDate(r.getParameter("signedDate"));
        x.setStartDate(r.getParameter("startDate"));
        x.setEndDate(r.getParameter("endDate"));
        x.setPaymentTerms(r.getParameter("paymentTerms"));
        x.setBusinessStatus(r.getParameter("businessStatus"));
        x.setCreateUserId(user(r).getId());
        x.setRemarks(r.getParameter("remarks"));
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
/** GET 处理分页列表、详情和新增/编辑表单。 */
// 列表支持按合同业务状态筛选，每页 10 条。
// 详情页按数据权限查询合同与产品明细。
// 合同不存在或当前用户无权查看时返回 404。
// /edit 加载已有合同；新增时尝试根据 opportunityId 预填。
// 无论新增还是编辑，表单都需要客户和产品选项。
/** POST 逻辑删除或保存合同主从表。 */
// 保存前校验客户归属，Service 再执行明细、日期和重复合同校验。
/** 根据已成交商机生成一个尚未入库的合同表单对象。 */
// 没有 opportunityId 时就是普通空白新增表单。
// 只有商机存在、状态为已成交且还没有合同时才允许预填。
// 产品明细直接从商机对象带入，用户仍可在保存前调整。
/** 将合同表单主字段与动态明细封装成 Contract。 */
// 创建人强制取当前 Session 用户，不接受前端传值。
/** 按相同索引组合产品、数量和单价三组数组参数。 */
// 没有产品行时返回空列表，Service 会拒绝无明细合同。
// 跳过未选择产品的空行。
// 对缺失的数量使用默认值 1，单价使用 BigDecimal 解析。
/** 客户归属校验：全局角色直接通过，销售员需在自己的客户列表中找到该客户。 */
