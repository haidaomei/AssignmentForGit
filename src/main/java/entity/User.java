package entity;

/**
 * 系统用户实体。
 *
 * <p>
 * “实体类”可以理解为一张数据库表在 Java 中的表示：一个 {@code User} 对象对应 {@code sys_user}
 * 表中的一行数据。字段使用 private，外部代码通过
 * getter 读取、通过 setter 写入， 这样既符合 JavaBean 规范，也方便 JSP 的 EL 表达式读取，例如 {@code
 * ${sessionScope.user.realName}}。
 */
public class User
{
    /**
     * 数据库自增主键，只用于程序内部关联，不直接展示给用户。
     */
    private Integer id;

    /**
     * 登录用户名，对应数据库 username 列。
     */
    private String username;

    /**
     * 登录密码；本教学项目按需求明文保存，生产项目必须改为不可逆哈希。
     */
    private String password;

    /**
     * 用户真实姓名，用于页面显示负责人和跟进人。
     */
    private String realName;

    /**
     * 联系电话。
     */
    private String phone;

    /**
     * 电子邮箱。
     */
    private String email;

    /**
     * 角色编码：admin、sales_manager 或 sales。
     */
    private String role;

    /**
     * 逻辑状态：1 表示启用，0 表示禁用。
     */
    private Integer status;

    // 以下 getter 用于“读取字段”，setter 用于“给字段赋值”。JSP/DAO 都依赖这些方法。

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer v)
    {
        id = v;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String v)
    {
        username = v;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String v)
    {
        password = v;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String v)
    {
        realName = v;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String v)
    {
        phone = v;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String v)
    {
        email = v;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String v)
    {
        role = v;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer v)
    {
        status = v;
    }

    public boolean isAdmin()
    {
        // 常量写在 equals 左边，即使 role 为 null 也不会抛出空指针异常。
        return "admin".equals(role);
    }

    public boolean isManager()
    {
        return "sales_manager".equals(role);
    }

    public boolean isSales()
    {
        return "sales".equals(role);
    }

    public String getRoleName()
    {
        // 三元运算符相当于简短的 if/else：把数据库中的英文角色编码转换为中文名称。
        return isAdmin() ? "管理员" : isManager() ? "销售经理" : "销售员";
    }
}
