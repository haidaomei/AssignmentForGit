package entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同主表实体，对应 {@code crm_contract}。
 *
 * <p>
 * 这是第二组主从表的主表。合同可以由已成交商机生成，也可以不关联商机而手工创建。 businessStatus 是合同业务状态，status
 * 是逻辑删除状态；items 保存合同产品明细。
 */
public class Contract
{
    /** 主键、关联商机、关联客户、创建人和逻辑状态。 */
    private Integer id, opportunityId, customerId, createUserId, status;

    /** 是否在未来 30 天内到期；该值用于列表高亮，不是数据库原始列。 */
    private boolean expiring;

    /** 合同编号、名称、关联展示名称、日期、条款、业务状态和备注。 */
    private String contractNo, contractName, opportunityTitle, customerName, signedDate, startDate, endDate, paymentTerms, businessStatus, attachmentPath, createUserName, remarks, createTime;

    /** 所有有效明细行的小计之和。 */
    private BigDecimal contractAmount;

    /** 合同产品明细集合，默认创建空列表以避免空指针。 */
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

    public Integer getOpportunityId()
    {
        return opportunityId;
    }

    public void setOpportunityId(Integer v)
    {
        opportunityId = v;
    }

    public Integer getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Integer v)
    {
        customerId = v;
    }

    public Integer getCreateUserId()
    {
        return createUserId;
    }

    public void setCreateUserId(Integer v)
    {
        createUserId = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public boolean isExpiring()
    {
        return expiring;
    }

    public void setExpiring(boolean v)
    {
        expiring = v;
    }

    public String getContractNo()
    {
        return contractNo;
    }

    public void setContractNo(String v)
    {
        contractNo = v;
    }

    public String getContractName()
    {
        return contractName;
    }

    public void setContractName(String v)
    {
        contractName = v;
    }

    public String getOpportunityTitle()
    {
        return opportunityTitle;
    }

    public void setOpportunityTitle(String v)
    {
        opportunityTitle = v;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String v)
    {
        customerName = v;
    }

    public String getSignedDate()
    {
        return signedDate;
    }

    public void setSignedDate(String v)
    {
        signedDate = v;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String v)
    {
        startDate = v;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public void setEndDate(String v)
    {
        endDate = v;
    }

    public String getPaymentTerms()
    {
        return paymentTerms;
    }

    public void setPaymentTerms(String v)
    {
        paymentTerms = v;
    }

    public String getBusinessStatus()
    {
        return businessStatus;
    }

    public void setBusinessStatus(String v)
    {
        businessStatus = v;
    }

    public String getAttachmentPath()
    {
        return attachmentPath;
    }

    public void setAttachmentPath(String v)
    {
        attachmentPath = v;
    }

    public String getCreateUserName()
    {
        return createUserName;
    }

    public void setCreateUserName(String v)
    {
        createUserName = v;
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

    public BigDecimal getContractAmount()
    {
        return contractAmount;
    }

    public void setContractAmount(BigDecimal v)
    {
        contractAmount = v;
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
