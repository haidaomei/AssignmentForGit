package entity;

/**
 * 客户转移审计记录，对应 {@code crm_customer_transfer_log}。
 *
 * <p>
 * 它只追加、不删除，用来回答“哪个客户在什么时间从谁转给了谁、为什么转移”。
 */
public class TransferLog
{
    /**
     * 日志主键、客户主键、原负责人和新负责人主键。
     */
    private Integer id, customerId, fromUserId, toUserId;

    /**
     * JOIN 得到的负责人姓名、格式化时间和转移原因。
     */
    private String fromUserName, toUserName, transferTime, reason;

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

    public Integer getFromUserId()
    {
        return fromUserId;
    }

    public void setFromUserId(Integer v)
    {
        fromUserId = v;
    }

    public Integer getToUserId()
    {
        return toUserId;
    }

    public void setToUserId(Integer v)
    {
        toUserId = v;
    }

    public String getFromUserName()
    {
        return fromUserName;
    }

    public void setFromUserName(String v)
    {
        fromUserName = v;
    }

    public String getToUserName()
    {
        return toUserName;
    }

    public void setToUserName(String v)
    {
        toUserName = v;
    }

    public String getTransferTime()
    {
        return transferTime;
    }

    public void setTransferTime(String v)
    {
        transferTime = v;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String v)
    {
        reason = v;
    }
}
