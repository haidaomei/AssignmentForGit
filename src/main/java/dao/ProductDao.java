package dao;

import entity.Product;

import java.sql.*;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 产品表 DAO。
 *
 * <p>
 * 查询只返回 status=1 的上架产品；所谓“删除”实际是把 status 更新为 0，历史商机和合同 仍通过明细中的产品名称快照显示原数据。
 */
public class ProductDao
{
    /**
     * JdbcTemplate 用于无需手工事务的查询。
     */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /**
     * 把产品查询结果的一行映射成 Product 对象。
     */
    private final RowMapper<Product> mapper = new RowMapper<Product>()
    {
        @Override
        public Product mapRow(ResultSet r, int n) throws SQLException
        {
            Product x = new Product();
            x.setId(r.getInt("id"));
            x.setProductName(r.getString("product_name"));
            x.setCategory(r.getString("category"));
            x.setUnit(r.getString("unit"));
            x.setUnitPrice(r.getBigDecimal("unit_price"));
            x.setDescription(r.getString("description"));
            x.setStatus(r.getInt("status"));
            x.setCreateTime(r.getString("created"));
            return x;
        }
    };

    /**
     * 列表、详情和下拉框共用的 SELECT 部分；DATE_FORMAT 直接生成页面需要的日期字符串。
     */
    private static final String SELECT = "SELECT p.*,DATE_FORMAT(p.create_time,'%Y-%m-%d') created FROM crm_product p ";

    /**
     * 统计名称模糊匹配的产品总数，用于计算分页页数。
     */
    public int count(String keyword)
    {
        Integer n = tpl.queryForObject("SELECT COUNT(*) FROM crm_product WHERE status=1 AND product_name LIKE ?", Integer.class, "%" + (keyword == null ? "" : keyword.trim()) + "%");
        return n == null ? 0 : n;
    }

    /**
     * 使用 LIMIT offset,size 查询某一页。offset 是跳过条数，不是页码。
     */
    public List<Product> page(int offset, int size, String keyword)
    {
        return tpl.query(SELECT + "WHERE status=1 AND product_name LIKE ? ORDER BY create_time DESC LIMIT ?,?", mapper, "%" + (keyword == null ? "" : keyword) + "%", offset, size);
    }

    /**
     * 查询全部上架产品，主要供商机/合同动态明细下拉框使用。
     */
    public List<Product> all()
    {
        return tpl.query(SELECT + "WHERE status=1 ORDER BY category,product_name", mapper);
    }

    /**
     * 按主键查询一个产品；查不到时返回 null，而不是让异常传到 Servlet。
     */
    public Product findById(int id)
    {
        try
        {
            return tpl.queryForObject(SELECT + "WHERE status=1 AND id=?", mapper, id);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    /**
     * 用 Service 传入的事务连接新增产品。
     */
    public int save(Connection conn, Product x) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("INSERT INTO crm_product(product_name,category,unit,unit_price,description,status)" + " VALUES(?,?,?,?,?,1)"))
        {
            bind(p, x);
            return p.executeUpdate();
        }
    }

    /**
     * 修改产品资料，只允许更新尚未逻辑删除的记录。
     */
    public int update(Connection conn, Product x) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_product SET" + " product_name=?,category=?,unit=?,unit_price=?,description=?,update_time=NOW()" + " WHERE id=? AND status=1"))
        {
            bind(p, x);
            p.setInt(6, x.getId());
            return p.executeUpdate();
        }
    }

    /**
     * 集中绑定新增和修改共有的五个参数，避免两个方法重复写 setXxx。
     */
    private void bind(PreparedStatement p, Product x) throws SQLException
    {
        p.setString(1, x.getProductName());
        p.setString(2, x.getCategory());
        p.setString(3, x.getUnit());
        p.setBigDecimal(4, x.getUnitPrice());
        p.setString(5, x.getDescription());
    }

    /**
     * 逻辑上架/下架：只修改 status，永远不执行 DELETE FROM。
     */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_product SET status=?,update_time=NOW() WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }
}