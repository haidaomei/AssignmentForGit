package entity;

/**
 * 客户实体，对应 {@code crm_customer} 表。
 *
 * <p>
 * 本类既保存客户表本身的字段，也保存关联查询得到的等级名称、来源名称和负责人姓名。 需求规定日期时间在实体中使用 String，因此 DAO 会先用
 * DATE_FORMAT
 * 把数据库时间格式化后再写入本对象。
 */
public class Customer
{
    /** 主键、三个外键、逻辑状态以及计算出来的未跟进天数。Integer 可以表达数据库中的 null。 */
    private Integer id, levelId, sourceId, ownerUserId, status, warningDays;

    /** 客户表直接保存的文字字段。customerNo 是对用户有意义的业务编号。 */
    private String customerNo, customerName, industry, scale, province, city, address, website, creditRating, description;

    /** JOIN 关联查询和日期格式化后得到的展示字段，它们不需要在表单中直接提交。 */
    private String levelName, sourceName, ownerName, lastFollowTime, createTime;

    // 标准 JavaBean getter/setter：DAO 用 setter 装入数据，JSP 用 getter 读取数据。

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer v)
    {
        id = v;
    }

    public Integer getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Integer v)
    {
        levelId = v;
    }

    public Integer getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(Integer v)
    {
        sourceId = v;
    }

    public Integer getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer v)
    {
        ownerUserId = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public Integer getWarningDays()
    {
        return warningDays;
    }

    public void setWarningDays(Integer v)
    {
        warningDays = v;
    }

    public String getCustomerNo()
    {
        return customerNo;
    }

    public void setCustomerNo(String v)
    {
        customerNo = v;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String v)
    {
        customerName = v;
    }

    public String getIndustry()
    {
        return industry;
    }

    public void setIndustry(String v)
    {
        industry = v;
    }

    public String getScale()
    {
        return scale;
    }

    public void setScale(String v)
    {
        scale = v;
    }

    public String getProvince()
    {
        return province;
    }

    public void setProvince(String v)
    {
        province = v;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String v)
    {
        city = v;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String v)
    {
        address = v;
    }

    public String getWebsite()
    {
        return website;
    }

    public void setWebsite(String v)
    {
        website = v;
    }

    public String getCreditRating()
    {
        return creditRating;
    }

    public void setCreditRating(String v)
    {
        creditRating = v;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String v)
    {
        description = v;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public void setLevelName(String v)
    {
        levelName = v;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String v)
    {
        sourceName = v;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String v)
    {
        ownerName = v;
    }

    public String getLastFollowTime()
    {
        return lastFollowTime;
    }

    public void setLastFollowTime(String v)
    {
        lastFollowTime = v;
    }

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String v)
    {
        createTime = v;
    }
}
