package entity;

import java.math.BigDecimal;

/**
 * 销售漏斗中的一个阶段数据，例如“需求分析：3 个商机，共 120000 元”。 Gson 会把 getter 对应的字段转成 JSON，供 ECharts
 * 在浏览器中绘图。
 */
public class FunnelData
{
    /** 阶段名称。 */
    private String name;

    /** 此阶段的有效商机数量。 */
    private int value;

    /** 此阶段预计金额合计。 */
    private BigDecimal amount;

    // 标准 JavaBean getter/setter。

    public String getName()
    {
        return name;
    }

    public void setName(String v)
    {
        name = v;
    }

    public int getValue()
    {
        return value;
    }

    public void setValue(int v)
    {
        value = v;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal v)
    {
        amount = v;
    }
}
