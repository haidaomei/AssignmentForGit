package entity;

/**
 * 通用字典选项。
 *
 * <p>
 * 客户等级、线索来源、商机阶段的数据结构相似，因此使用这个轻量对象作为下拉框选项。 商机阶段额外使用 sortOrder 控制顺序，使用
 * probability 保存默认成交概率。
 */
public class Lookup
{
    /** 字典记录主键。 */
    private Integer id;

    /** 页面显示的中文名称。 */
    private String name;

    /** 程序使用的稳定英文编码。 */
    private String code;

    /** 数字越小越靠前。 */
    private Integer sortOrder;

    /** 商机阶段对应的默认成交概率。 */
    private Integer probability;

    /** 无参构造器是 JavaBean 规范的一部分，框架创建对象时会使用它。 */
    public Lookup()
    {
    }

    /** 便捷构造器：只有主键和名称时可以一次完成赋值。 */
    public Lookup(Integer id, String name)
    {
        this.id = id;
        this.name = name;
    }

    // 标准 JavaBean getter/setter。
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer v)
    {
        id = v;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String v)
    {
        name = v;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String v)
    {
        code = v;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer v)
    {
        sortOrder = v;
    }

    public Integer getProbability()
    {
        return probability;
    }

    public void setProbability(Integer v)
    {
        probability = v;
    }
}
