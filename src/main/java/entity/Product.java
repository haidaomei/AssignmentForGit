package entity;

import java.math.BigDecimal;

/**
 * 产品或服务实体，对应 {@code crm_product} 表。
 *
 * <p>
 * 产品会被商机明细和合同明细引用。金额使用 BigDecimal 而不是 double，避免二进制浮点数 在财务计算中出现 0.1 + 0.2
 * 不精确的问题。
 */
public class Product
{
    /** 数据库主键和上架状态：1 为上架，0 为下架。 */
    private Integer id, status;

    /** 产品名称、分类、计量单位、说明及格式化后的创建日期。 */
    private String productName, category, unit, description, createTime;

    /** 标准单价，使用十进制精确类型。 */
    private BigDecimal unitPrice;

    // 标准 JavaBean getter/setter。

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer v)
    {
        id = v;
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

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String v)
    {
        category = v;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String v)
    {
        unit = v;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String v)
    {
        description = v;
    }

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String v)
    {
        createTime = v;
    }

    public BigDecimal getUnitPrice()
    {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal v)
    {
        unitPrice = v;
    }
}
