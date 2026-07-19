package entity;

/**
 * 跟进记录实体，对应 {@code crm_follow_up_record} 表。
 *
 * <p>
 * 每条记录必须关联客户，可以选择关联商机和联系人。保存跟进时 Service 还会同步更新客户的
 * last_follow_time，这两个写操作必须处于同一个事务中。
 */
public class FollowUp
{
    /**
     * 主键和关联外键、是否已提醒、逻辑状态。
     */
    private Integer id, customerId, opportunityId, contactId, followUserId, isReminded, status;

    /**
     * 由 SQL 根据 next_follow_time 和当前时间计算，true 时列表行标黄。
     */
    private boolean overdue;

    /**
     * 页面展示和跟进业务所需的文字、日期时间字段。
     */
    private String customerName, opportunityTitle, contactName, followUserName, followType, followContent, customerFeedback, nextPlan, nextFollowTime, followTime, createTime;

    // 标准 JavaBean getter/setter。
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

    public Integer getOpportunityId()
    {
        return opportunityId;
    }

    public void setOpportunityId(Integer v)
    {
        opportunityId = v;
    }

    public Integer getContactId()
    {
        return contactId;
    }

    public void setContactId(Integer v)
    {
        contactId = v;
    }

    public Integer getFollowUserId()
    {
        return followUserId;
    }

    public void setFollowUserId(Integer v)
    {
        followUserId = v;
    }

    public Integer getIsReminded()
    {
        return isReminded;
    }

    public void setIsReminded(Integer v)
    {
        isReminded = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public boolean isOverdue()
    {
        return overdue;
    }

    public void setOverdue(boolean v)
    {
        overdue = v;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String v)
    {
        customerName = v;
    }

    public String getOpportunityTitle()
    {
        return opportunityTitle;
    }

    public void setOpportunityTitle(String v)
    {
        opportunityTitle = v;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String v)
    {
        contactName = v;
    }

    public String getFollowUserName()
    {
        return followUserName;
    }

    public void setFollowUserName(String v)
    {
        followUserName = v;
    }

    public String getFollowType()
    {
        return followType;
    }

    public void setFollowType(String v)
    {
        followType = v;
    }

    public String getFollowContent()
    {
        return followContent;
    }

    public void setFollowContent(String v)
    {
        followContent = v;
    }

    public String getCustomerFeedback()
    {
        return customerFeedback;
    }

    public void setCustomerFeedback(String v)
    {
        customerFeedback = v;
    }

    public String getNextPlan()
    {
        return nextPlan;
    }

    public void setNextPlan(String v)
    {
        nextPlan = v;
    }

    public String getNextFollowTime()
    {
        return nextFollowTime;
    }

    public void setNextFollowTime(String v)
    {
        nextFollowTime = v;
    }

    public String getFollowTime()
    {
        return followTime;
    }

    public void setFollowTime(String v)
    {
        followTime = v;
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
