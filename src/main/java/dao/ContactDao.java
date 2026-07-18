package dao;

import entity.Contact;
import java.sql.*;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import util.DbHelper;

/**
 * 联系人表 DAO。
 *
 * <p>
 * 所有查询都 JOIN 客户表并同时检查 ct.status=1 与 c.status=1。这样客户被逻辑删除后， 即使联系人自身仍是
 * 1，也会按照“父记录删除后子记录级联隐藏”的规则从页面消失。
 */
public class ContactDao
{
    /** 只读查询工具。 */
    private final JdbcTemplate tpl = DbHelper.getJdbcTemplate();

    /** 共用 SELECT：关联客户名称，并把创建时间格式化成字符串。 */
    private static final String SELECT = "SELECT ct.*,c.customer_name,DATE_FORMAT(ct.create_time,'%Y-%m-%d') created FROM crm_contact" + " ct JOIN crm_customer c ON ct.customer_id=c.id ";
    //先提前说,sql允许别名在语句字符串后面定义,前面调用,上面在FROM定义了crm_contact的别名为ct,在SELECT用ct
    //这个sql的套路:(去别名,不然不好看)
    //SELECT crm_contact.*,
    //crm_customer.customer_name,
    //DATE_FORMAT(ct.create_time,'%Y-%m-%d')
    //FROM crm_contact
    //JOIN crm_customer
    //ON crm_contact.customer_id=crm_customer.id;
    //回顾一下ER图,customer对contact是一对多,contact有一外键为customer的id,也即contact依赖于customer
    //当customer被逻辑删除删除,contact是不能被显示的
    //这里将customer和contact合并成一张表,既然一个customer可以持有多个contact,那么合并之后可以是
    //表customer|头contact|其他属性
    //customer1|contact1
    //customer1|contact2
    //customer1|contact3
    //customer2|contact4
    //customer2|contact5
    //customer2|contact6
    //这个时候我们再去查两者状态相等才能显示的(主要是在下面的方法,这里不写),输出主要是输出合并后的表的contact元素,加一个cunstomer的名字
    //以上就是级联不显示的套路,contact依赖customer,外键引父母主键,join(笛卡尔积)两表,过滤错数据,求未删状态
    //上面返回的是一张表,只返回一张join后的表,怎么发挥下面随意
    //比如通过customer找,通过主键找,通过分页找
    //这里的contact和数据库里的contact有差异,java数据结构里的contac还多了一个customerName,服务于这里共用SELECT的返回

    /** 显式 RowMapper，逐列说明数据库结果如何装入 Contact。 */
    private final RowMapper<Contact> mapper = new RowMapper<Contact>()
    {
        @Override
        public Contact mapRow(ResultSet r, int n) throws SQLException
        {
            Contact x = new Contact();
            x.setId(r.getInt("id"));
            x.setCustomerId(r.getInt("customer_id"));
            x.setName(r.getString("name"));
            x.setCustomerName(r.getString("customer_name"));
            x.setGender(r.getString("gender"));
            x.setPosition(r.getString("position"));
            x.setPhone(r.getString("phone"));
            x.setEmail(r.getString("email"));
            x.setWechat(r.getString("wechat"));
            x.setIsPrimary(r.getInt("is_primary"));
            x.setIsDecisionMaker(r.getInt("is_decision_maker"));
            x.setHobby(r.getString("hobby"));
            x.setRemarks(r.getString("remarks"));
            x.setStatus(r.getInt("status"));
            x.setCreateTime(r.getString("created"));
            return x;
        }
    };
    //注意匿名类语法,这里定义一个匿名类RowMapper<T>{方法{}};,之后通过new Type()的方式来获得一个匿名类的对象
    //对于mapper对象,目前可以不必深究其方法输入输出,当做在tpl.query时传入的一个参数,控制sql语句返回的表的每个元组对目标List其中每个对象的setter即可
    //这里顺便约定,以后讲sql统一讲元组,讲面向对象语言统一讲对象,以便在二者结合的场景做区分

    private String scope(boolean sales)
    {
        //销售员只能看到自己负责客户的联系人,管理员/经理不追加这个条件
        //这里注意,这也是统合了很多方法,所以下面看到为什么tpl.query里的参数多了,那肯定是调用这里了
        return sales ? " AND c.owner_user_id=? " : "";
    }

    /** 统计搜索结果数量；姓名和电话都支持模糊匹配。 */
    public int count(String keyword, Integer uid, boolean sales)
    {
        String sql = "SELECT COUNT(*) FROM crm_contact ct JOIN crm_customer c ON ct.customer_id=c.id WHERE" + " ct.status=1 AND c.status=1 AND (ct.name LIKE ? OR ct.phone LIKE ?)" + scope(sales);
        String k = "%" + (keyword == null ? "" : keyword) + "%";
        Integer n = sales ? tpl.queryForObject(sql, Integer.class, k, k, uid) : tpl.queryForObject(sql, Integer.class, k, k);
        //queryforobj和query的区别是,编码者需要预知这条sql返回的是一张表还是一个元组
        //(虽然一个元组也算一张表 但是它们在tpl方法上就是有区别的)
        //当返回的只有一个元组,则用queryforobj
        //queryforobj和query区别不大,不讨论它们返回的数据类型,query能用在只返回一个元组的场景,queryforobj也能用在结果为多元组的场景
        //区别是,queryforobj返回的是int或一个元组在mapper里对应的对象(有limit1的意味),而query无论如何都会返回一个List
        //还需要知道的是,上面requiredType写Integer.class,这是预知了返回可以是一个数字(因为sql里就是聚合函数)
        //但是如果返回一个元组还写requeiredType是Integer.class,那么会爆,类型不匹配
        return n == null ? 0 : n;
    }

    /** 查询联系人分页数据，sales=true 时 SQL 会多绑定一个当前用户主键。 */
    public List<Contact> page(int offset, int size, String keyword, Integer uid, boolean sales)
    {
        String sql = SELECT + "WHERE ct.status=1 AND c.status=1 AND (ct.name LIKE ? OR ct.phone LIKE ?)" + scope(sales) + " ORDER BY ct.create_time DESC LIMIT ?,?";
        String k = "%" + (keyword == null ? "" : keyword) + "%";
        return sales ? tpl.query(sql, mapper, k, k, uid, offset, size) : tpl.query(sql, mapper, k, k, offset, size);
    }

    /** 查询某个有效客户下的全部有效联系人，主联系人排在最前面。 */
    public List<Contact> byCustomer(int customerId)
    {
        return tpl.query(SELECT + "WHERE ct.status=1 AND c.status=1 AND ct.customer_id=? ORDER BY ct.is_primary" + " DESC,ct.name", mapper, customerId);
    }

    /** 按主键查询联系人，并继续应用销售员数据范围条件。 */
    public Contact findById(int id, Integer uid, boolean sales)
    {
        try
        {
            String sql = SELECT + "WHERE ct.status=1 AND c.status=1 AND ct.id=?" + scope(sales);
            return sales ? tpl.queryForObject(sql, mapper, id, uid) : tpl.queryForObject(sql, mapper, id);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    //以下的操作设计增删改,通过事务实现,上面spring封装jdbc的jdbctemplate只负责查,这里的事务由原生的jdbc实现(除了druid连接池)
    //这也是上面为什么没用到所谓conn
    //这个对象流转方式可以参考之前的项目,多个窗口对一个APIClient进行复用,LoginWindow里new一个APIClient,当登录结束后LoginWindow关闭,这个APIClient传递给MainWindow使用去,不new一个新的
    //之前的项目没讨论过new一个新的是否有问题,但这里在DAO层new一个conn绝对有问题,因为同一个事务的操作必须同属一个conn,当调用多个DAO,如果每个DAO都new一个conn并通过自己的conn执行
    //那么这些所有语句看似包装在事务里,实则不在同一个事务,这和setautocommit开了没什么区别
    
    //java只有一种传参方式:传值(pass-by-value),对于对象来说,传的前后指向全局同一个对象(可以理解为对象全传引用,当然远不止于此,目前理解即可)
    //事务执行的流程:定义sql,将sql扔进conn的preparestatement,使用preparestatement的set类型(参数位置,传入的参数)语法给sql传入参数,之后对preparestatement对象执行executeupdate方法

    /** 新增联系人。Connection 来自 Service，保证它参与 Service 控制的事务。 */
    public int save(Connection conn, Contact x) throws SQLException
    {
        String sql = "INSERT INTO" + " crm_contact(customer_id,name,gender,position,phone,email,wechat,is_primary,is_decision_maker,hobby,remarks,status)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?,1)";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, x);
            return p.executeUpdate();
        }
    }

    /** 更新联系人；WHERE status=1 防止修改已经逻辑删除的数据。 */
    public int update(Connection conn, Contact x) throws SQLException
    {
        String sql = "UPDATE crm_contact SET" + " customer_id=?,name=?,gender=?,position=?,phone=?,email=?,wechat=?,is_primary=?,is_decision_maker=?,hobby=?,remarks=?,update_time=NOW()" + " WHERE id=? AND status=1";
        try (PreparedStatement p = conn.prepareStatement(sql))
        {
            bind(p, x);
            p.setInt(12, x.getId());
            return p.executeUpdate();
        }
    }

    /** 按问号顺序绑定联系人字段；复选框在实体中已转换成 0 或 1。 */
    private void bind(PreparedStatement p, Contact x) throws SQLException
    {
        p.setInt(1, x.getCustomerId());
        p.setString(2, x.getName());
        p.setString(3, x.getGender());
        p.setString(4, x.getPosition());
        p.setString(5, x.getPhone());
        p.setString(6, x.getEmail());
        p.setString(7, x.getWechat());
        p.setInt(8, x.getIsPrimary() == null ? 0 : x.getIsPrimary());
        p.setInt(9, x.getIsDecisionMaker() == null ? 0 : x.getIsDecisionMaker());
        p.setString(10, x.getHobby());
        p.setString(11, x.getRemarks());
    }

    /** 逻辑删除或恢复联系人。 */
    public int updateStatus(Connection conn, int id, int status) throws SQLException
    {
        try (PreparedStatement p = conn.prepareStatement("UPDATE crm_contact SET status=?,update_time=NOW() WHERE id=?"))
        {
            p.setInt(1, status);
            p.setInt(2, id);
            return p.executeUpdate();
        }
    }
}
