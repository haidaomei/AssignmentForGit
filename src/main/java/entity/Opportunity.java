package entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商机主表实体，对应 {@code crm_business_opportunity}。
 *
 * <p>
 * 它是本系统第一组主从表的“主表”：基本信息存在本对象，产品明细存在 items 集合。 status 专门表示逻辑删除，businessStatus
 * 表示“进行中/已成交/已丢单”，两者不可混用。
 */
public class Opportunity
{
    /**
     * 主键和各关联表外键，以及成交概率、逻辑状态、阶段排序值。
     */
    private Integer id, customerId, contactId, stageId, ownerUserId, probability, status, stageSort;

    /**
     * 业务编号、标题、关联名称、日期、描述、结果原因和业务状态。
     */
    private String opportunityNo, title, customerName, contactName, stageName, ownerName, estimatedCloseDate, description, resultReason, businessStatus, createTime;

    /**
     * 产品明细汇总后的预计金额。
     */
    private BigDecimal expectedAmount;

    /**
     * 主从关系中的“从表”数据；初始化为空集合可避免调用方遇到 null。
     */
    private List<LineItem> items = new ArrayList<>();

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

    public Integer getContactId()
    {
        return contactId;
    }

    public void setContactId(Integer v)
    {
        contactId = v;
    }

    public Integer getStageId()
    {
        return stageId;
    }

    public void setStageId(Integer v)
    {
        stageId = v;
    }

    public Integer getOwnerUserId()
    {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer v)
    {
        ownerUserId = v;
    }

    public Integer getProbability()
    {
        return probability;
    }

    public void setProbability(Integer v)
    {
        probability = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public Integer getStageSort()
    {
        return stageSort;
    }

    public void setStageSort(Integer v)
    {
        stageSort = v;
    }

    public String getOpportunityNo()
    {
        return opportunityNo;
    }

    public void setOpportunityNo(String v)
    {
        opportunityNo = v;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String v)
    {
        title = v;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String v)
    {
        customerName = v;
    }

    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String v)
    {
        contactName = v;
    }

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String v)
    {
        stageName = v;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String v)
    {
        ownerName = v;
    }

    public String getEstimatedCloseDate()
    {
        return estimatedCloseDate;
    }

    public void setEstimatedCloseDate(String v)
    {
        estimatedCloseDate = v;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String v)
    {
        description = v;
    }

    public String getResultReason()
    {
        return resultReason;
    }

    public void setResultReason(String v)
    {
        resultReason = v;
    }

    public String getBusinessStatus()
    {
        return businessStatus;
    }

    public void setBusinessStatus(String v)
    {
        businessStatus = v;
    }

    public String getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(String v)
    {
        createTime = v;
    }

    public BigDecimal getExpectedAmount()
    {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal v)
    {
        expectedAmount = v;
    }

    public List<LineItem> getItems()
    {
        return items;
    }

    public void setItems(List<LineItem> v)
    {
        items = v;
    }
}
