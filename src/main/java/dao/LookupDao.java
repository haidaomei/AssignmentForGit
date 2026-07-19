package dao;

import entity.Lookup;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 静态字典数据访问对象。
 *
 * <p>
 * 客户等级、线索来源和商机阶段基本不会在业务流程中修改，所以这里只提供查询。 三类字典最终都映射为 Lookup，减少重复实体和重复页面代码。
 */
public class LookupDao
{
    /**
     * 复用全局 JdbcTemplate 执行只读 SELECT。
     */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /**
     * 创建一个可复用 RowMapper。不同字典的“名称列”和“编码列”名字不同，因此通过参数传入列名。
     * 这里的列名来自程序内部常量，不来自用户输入，所以不会造成 SQL 注入。
     */
    private RowMapper<Lookup> mapper(final String nameColumn, final String codeColumn)
    {
        return new RowMapper<Lookup>()
        {
            @Override
            public Lookup mapRow(ResultSet rs, int rowNum) throws SQLException
            {
                Lookup x = new Lookup();
                x.setId(rs.getInt("id"));
                x.setName(rs.getString(nameColumn));
                x.setCode(rs.getString(codeColumn));
                return x;
            }
        };
    }

    /**
     * 按 sort_order 查询客户等级，保证 VIP、重点、普通、潜在按规定顺序显示。
     */
    public List<Lookup> levels()
    {
        return tpl.query("SELECT id,level_name,level_code FROM crm_customer_level ORDER BY sort_order", mapper("level_name", "level_code"));
    }

    /**
     * 查询全部线索来源，供客户表单下拉框使用。
     */
    public List<Lookup> sources()
    {
        return tpl.query("SELECT id,source_name,source_code FROM crm_lead_source ORDER BY id", mapper("source_name", "source_code"));
    }

    /**
     * 查询商机阶段；除名称编码外，还读取阶段顺序和默认成交概率。
     */
    public List<Lookup> stages()
    {
        return tpl.query("SELECT id,stage_name,stage_code,sort_order,win_probability FROM crm_opportunity_stage" + " ORDER BY sort_order", new RowMapper<Lookup>()
        {
            @Override
            public Lookup mapRow(ResultSet rs, int n) throws SQLException
            {
                Lookup x = new Lookup();
                x.setId(rs.getInt("id"));
                x.setName(rs.getString("stage_name"));
                x.setCode(rs.getString("stage_code"));
                x.setSortOrder(rs.getInt("sort_order"));
                x.setProbability(rs.getInt("win_probability"));
                return x;
            }
        });
    }
}
