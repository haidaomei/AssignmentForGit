package dao;

import entity.DashboardStats;
import entity.FunnelData;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 仪表盘聚合统计 DAO。
 *
 * <p>
 * 本类不返回普通业务明细，而是使用 COUNT、SUM、GROUP BY 等聚合 SQL 计算经营指标。 sales
 * 参数决定统计全公司还是只统计当前销售员负责的数据。
 */
public class DashboardDao
{
    /** 所有统计查询共用的 JdbcTemplate。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /** 客户表别名为 c 时使用的数据范围条件。 */
    private String cScope(boolean sales)
    {
        return sales ? " AND c.owner_user_id=?" : "";
    }

    /** 商机表别名为 o 时使用的数据范围条件。 */
    private String oScope(boolean sales)
    {
        return sales ? " AND o.owner_user_id=?" : "";
    }

    /** 执行返回整数的统计 SQL，并统一把数据库 null 转为 0。 */
    private int integer(String sql, Integer uid, boolean sales)
    {
        Integer n = sales ? tpl.queryForObject(sql, Integer.class, uid) : tpl.queryForObject(sql, Integer.class);
        return n == null ? 0 : n;
    }

    /** 执行返回金额的统计 SQL，并统一把数据库 null 转为 BigDecimal.ZERO。 */
    private BigDecimal decimal(String sql, Integer uid, boolean sales)
    {
        BigDecimal n = sales ? tpl.queryForObject(sql, BigDecimal.class, uid) : tpl.queryForObject(sql, BigDecimal.class);
        return n == null ? BigDecimal.ZERO : n;
    }

    /** 依次计算六个仪表盘指标。 每个 SQL 都带 status=1，并在 sales=true 时额外绑定当前用户 id。 */
    public DashboardStats stats(Integer uid, boolean sales)
    {
        // 先创建空统计对象，再逐项写入查询结果。
        DashboardStats x = new DashboardStats();
        x.setCustomerCount(integer("SELECT COUNT(*) FROM crm_customer c WHERE c.status=1" + cScope(sales), uid, sales));
        x.setMonthCustomerCount(integer("SELECT COUNT(*) FROM crm_customer c WHERE c.status=1 AND" + " DATE_FORMAT(c.create_time,'%Y-%m')=DATE_FORMAT(CURDATE(),'%Y-%m')" + cScope(sales), uid, sales));
        x.setActiveOpportunityCount(integer("SELECT COUNT(*) FROM crm_business_opportunity o JOIN crm_customer c ON" + " o.customer_id=c.id WHERE o.status=1 AND c.status=1 AND o.business_status='进行中'" + oScope(sales), uid, sales));
        x.setExpectedAmount(decimal("SELECT COALESCE(SUM(o.expected_amount),0) FROM crm_business_opportunity o JOIN" + " crm_customer c ON o.customer_id=c.id WHERE o.status=1 AND c.status=1 AND" + " o.business_status='进行中'" + oScope(sales), uid, sales));
        x.setTodayTodoCount(integer("SELECT COUNT(*) FROM crm_follow_up_record f JOIN crm_customer c ON f.customer_id=c.id" + " WHERE f.status=1 AND c.status=1 AND DATE(f.next_follow_time)=CURDATE() AND" + " f.is_reminded=0" + cScope(sales), uid, sales));
        x.setWarningCount(integer("SELECT COUNT(*) FROM crm_customer c WHERE c.status=1 AND (c.last_follow_time IS NULL" + " OR DATEDIFF(NOW(),c.last_follow_time)>30)" + cScope(sales), uid, sales));
        // 所有字段装好后一次性返回给 Service。
        return x;
    }

    /** 按商机阶段分组统计数量与金额。 LEFT JOIN 保证某阶段即使暂时没有商机也会返回 0；EXISTS 排除已删除客户的商机。 */
    public List<FunnelData> funnel(Integer uid, boolean sales)
    {
        String sql = "SELECT st.stage_name name,COUNT(o.id) value,COALESCE(SUM(o.expected_amount),0) amount FROM" + " crm_opportunity_stage st LEFT JOIN crm_business_opportunity o ON st.id=o.stage_id" + " AND o.status=1 AND EXISTS(SELECT 1 FROM crm_customer c WHERE c.id=o.customer_id AND" + " c.status=1)" + (sales ? " AND o.owner_user_id=?" : "") + " WHERE st.stage_code<>'LOST' GROUP BY st.id,st.stage_name,st.sort_order ORDER BY" + " st.sort_order";
        // 漏斗每一行只有阶段名称、数量、金额三个字段，使用专用的短 RowMapper。
        RowMapper<FunnelData> m = new RowMapper<FunnelData>()
        {
            @Override
            public FunnelData mapRow(ResultSet r, int n) throws SQLException
            {
                FunnelData x = new FunnelData();
                x.setName(r.getString("name"));
                x.setValue(r.getInt("value"));
                x.setAmount(r.getBigDecimal("amount"));
                return x;
            }
        };
        return sales ? tpl.query(sql, m, uid) : tpl.query(sql, m);
    }
}
