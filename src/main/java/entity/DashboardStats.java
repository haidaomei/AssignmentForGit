package entity;

import java.math.BigDecimal;

/**
 * 仪表盘六个统计卡片的数据集合。
 *
 * <p>
 * DAO 分别执行聚合 SQL，Service 把结果装入本对象，Servlet 再一次性传给 dashboard.jsp。
 */
public class DashboardStats
{
    /** 客户总数、本月新增、进行中商机、今日待办和预警客户数。 */
    private int customerCount, monthCustomerCount, activeOpportunityCount, todayTodoCount, warningCount;

    /** 进行中商机预计金额合计；初始化为 0，避免页面对 null 做金额格式化时报错。 */
    private BigDecimal expectedAmount = BigDecimal.ZERO;

    // 标准 JavaBean getter/setter。

    public int getCustomerCount()
    {
        return customerCount;
    }

    public void setCustomerCount(int v)
    {
        customerCount = v;
    }

    public int getMonthCustomerCount()
    {
        return monthCustomerCount;
    }

    public void setMonthCustomerCount(int v)
    {
        monthCustomerCount = v;
    }

    public int getActiveOpportunityCount()
    {
        return activeOpportunityCount;
    }

    public void setActiveOpportunityCount(int v)
    {
        activeOpportunityCount = v;
    }

    public int getTodayTodoCount()
    {
        return todayTodoCount;
    }

    public void setTodayTodoCount(int v)
    {
        todayTodoCount = v;
    }

    public int getWarningCount()
    {
        return warningCount;
    }

    public void setWarningCount(int v)
    {
        warningCount = v;
    }

    public BigDecimal getExpectedAmount()
    {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal v)
    {
        expectedAmount = v;
    }
}
