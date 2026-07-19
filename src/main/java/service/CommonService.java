package service;

import dao.LookupDao;
import dao.UserDao;
import entity.Lookup;
import entity.User;

import java.util.List;

/**
 * 页面公共数据业务类。
 *
 * <p>
 * Service（业务层）位于 Servlet 和 DAO 之间。多个页面都需要客户等级、客户来源、商机阶段和用户列表，
 * 因此把这些通用查询放在一个类中，避免各个 Servlet 重复调用
 * DAO。
 */
public class CommonService
{
    /**
     * 字典 DAO：负责查询等级、来源和阶段这类固定选项。
     */
    private final LookupDao lookupDao = new LookupDao();

    /**
     * 用户 DAO：负责查询可作为客户负责人的正常用户。
     */
    private final UserDao userDao = new UserDao();

    /**
     * @return 所有启用的客户等级，用于表单下拉框
     */
    public List<Lookup> levels()
    {
        return lookupDao.levels();
    }

    /**
     * @return 所有启用的客户来源，用于表单下拉框
     */
    public List<Lookup> sources()
    {
        return lookupDao.sources();
    }

    /**
     * @return 按排序值返回的商机阶段，用于创建商机和阶段推进
     */
    public List<Lookup> stages()
    {
        return lookupDao.stages();
    }

    /**
     * @return 所有未被逻辑删除的用户，用于负责人选择和客户转移
     */
    public List<User> users()
    {
        return userDao.findAllActive();
    }
}
