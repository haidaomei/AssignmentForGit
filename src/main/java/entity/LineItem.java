package entity;

import java.math.BigDecimal;

/**
 * 商机和合同共用的产品明细实体。
 *
 * <p>
 * 两张明细表字段完全一致，因此用一个 Java 类复用。parentId 在商机中表示 opportunity_id， 在合同中表示
 * contract_id；subtotal 等于
 * quantity × unitPrice，由 Service 统一计算，不能相信前端传来的合计值。
 */
public class LineItem
{
    /** 明细主键、所属主表、产品外键、购买数量和逻辑状态。 */
    private Integer id, parentId, productId, quantity, status;

    /** 保存产品名称快照，产品以后改名时历史单据仍能显示原名称。 */
    private String productName;

    /** 成交单价与行小计，均使用 BigDecimal 保证金额精度。 */
    private BigDecimal unitPrice, subtotal;

    // 标准 JavaBean getter/setter。

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer v)
    {
        id = v;
    }

    public Integer getParentId()
    {
        return parentId;
    }

    public void setParentId(Integer v)
    {
        parentId = v;
    }

    public Integer getProductId()
    {
        return productId;
    }

    public void setProductId(Integer v)
    {
        productId = v;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer v)
    {
        quantity = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String v)
    {
        productName = v;
    }

    public BigDecimal getUnitPrice()
    {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal v)
    {
        unitPrice = v;
    }

    public BigDecimal getSubtotal()
    {
        return subtotal;
    }

    public void setSubtotal(BigDecimal v)
    {
        subtotal = v;
    }
}
