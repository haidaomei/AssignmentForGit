package util;

import com.alibaba.druid.pool.DruidDataSource;
import java.io.InputStream;
import java.util.Properties;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 数据库连接池工厂，是整个项目访问数据库的统一入口。
 *
 * <p>
 * DruidDataSource 负责复用数据库连接，避免每次请求都重新创建昂贵的网络连接；JdbcTemplate
 * 封装了查询时的连接获取、Statement 创建和结果遍历。写操作仍由
 * Service 手工获取 Connection， 因为只有拿到同一个 Connection 才能把多条 SQL 放进同一个事务。
 */
public final class DbHelper
{
    /** 全局唯一的 Druid 连接池。static 表示它属于类，而不是某一个 DbHelper 对象。 */
    private static DruidDataSource dataSource;

    /** 基于同一连接池创建的查询工具，所有 DAO 共用。 */
    private static JdbcTemplate jdbcTemplate;

    // 静态代码块在 DbHelper 第一次被使用时只执行一次，用来完成全部数据库初始化工作。
    static
    {
        try
        {
            // Properties 专门读取 key=value 形式的配置，避免把账号密码写死在 Java 代码中。
            Properties props = new Properties();
            // getClassLoader 从 Maven 打包后的 WEB-INF/classes 中寻找 jdbc.properties。
            // try-with-resources 会在代码块结束时自动关闭输入流。
            try (InputStream is = DbHelper.class.getClassLoader().getResourceAsStream("jdbc.properties"))
            {
                // 如果返回 null，说明配置文件没有被打包或名称写错，应立即终止启动并给出明确提示。
                if (is == null)
                {
                    throw new RuntimeException("找不到 jdbc.properties 文件");
                }
                // 把配置文件中的每一项加载到 props 对象。
                props.load(is);
            }
            // 创建连接池对象，下面逐项把配置写进去。
            dataSource = new DruidDataSource();
            // JDBC 驱动类负责让 Java 能理解 MySQL 协议。
            dataSource.setDriverClassName(props.getProperty("driverClassName"));
            // URL 指明数据库地址、库名、字符集和时区。
            dataSource.setUrl(props.getProperty("url"));
            dataSource.setUsername(props.getProperty("username"));
            dataSource.setPassword(props.getProperty("password"));
            // 第二个参数是默认值：配置缺失时仍能使用一个安全的基础值启动。
            dataSource.setInitialSize(Integer.parseInt(props.getProperty("initialSize", "5")));
            dataSource.setMinIdle(Integer.parseInt(props.getProperty("minIdle", "5")));
            dataSource.setMaxActive(Integer.parseInt(props.getProperty("maxActive", "20")));
            dataSource.setMaxWait(Long.parseLong(props.getProperty("maxWait", "60000")));
            // 连接池执行 SELECT 1 来判断某条空闲连接是否仍然可用。
            dataSource.setValidationQuery("SELECT 1");
            dataSource.setTestWhileIdle(true);
            // JdbcTemplate 不创建新连接池，而是复用上面配置好的 dataSource。
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        catch (Exception e)
        {
            // 初始化失败时继续运行只会产生更多空指针，因此包装异常并阻止系统带病启动。
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }

    /** 工具类只提供静态方法，不应该被 new，所以构造器设为 private。 */
    private DbHelper()
    {
    }

    /** 返回查询专用 JdbcTemplate；DAO 的 SELECT 方法调用它。 */
    public static JdbcTemplate getJdbcTemplate()
    {
        return jdbcTemplate;
    }

    /** 返回原始连接池；Service 的写方法用它获取同一个事务连接。 */
    public static DruidDataSource getDataSource()
    {
        return dataSource;
    }
}
