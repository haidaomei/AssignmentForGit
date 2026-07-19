package entity;

/**
 * 联系人实体，对应 {@code crm_contact} 表。
 *
 * <p>
 * 一个客户可以拥有多个联系人。customerId 保存所属客户的主键，customerName 是 JOIN 查询后
 * 为页面准备的客户名称。isPrimary 和
 * isDecisionMaker 用 0/1 表示否/是。
 */
public class Contact
{
    /**
     * 主键、所属客户、两个布尔标志和逻辑删除状态。
     */
    private Integer id, customerId, isPrimary, isDecisionMaker, status;

    /**
     * 姓名、联系方式、职位、备注以及格式化后的创建时间。
     */
    private String name, customerName, gender, position, phone, email, wechat, hobby, remarks, createTime;

    // 以下方法遵循 JavaBean 规范，使 JdbcTemplate 映射和 JSP EL 能统一访问这些属性。
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer v)
    {
        id = v;
    }

    public Integer getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Integer v)
    {
        customerId = v;
    }

    public Integer getIsPrimary()
    {
        return isPrimary;
    }

    public void setIsPrimary(Integer v)
    {
        isPrimary = v;
    }

    public Integer getIsDecisionMaker()
    {
        return isDecisionMaker;
    }

    public void setIsDecisionMaker(Integer v)
    {
        isDecisionMaker = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String v)
    {
        name = v;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String v)
    {
        customerName = v;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String v)
    {
        gender = v;
    }

    public String getPosition()
    {
        return position;
    }

    public void setPosition(String v)
    {
        position = v;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String v)
    {
        phone = v;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String v)
    {
        email = v;
    }

    public String getWechat()
    {
        return wechat;
    }

    public void setWechat(String v)
    {
        wechat = v;
    }

    public String getHobby()
    {
        return hobby;
    }

    public void setHobby(String v)
    {
        hobby = v;
    }

    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String v)
    {
        remarks = v;
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
