package entity;

import java.util.List;

/**
 * 通用分页结果对象。
 *
 * <p>
 * {@code <T>} 是 Java 泛型，表示“一页中的元素类型暂时不确定”。例如 PageBean&lt;Customer&gt;
 * 表示客户页，PageBean&lt;Product&gt; 表示产品页。这样只写一个分页类就能服务所有列表。
 */
public class PageBean<T>
{
    /** 当前页，从 1 开始。 */
    private int currentPage;

    /** 每页最多显示多少条。 */
    private int pageSize;

    /** 数据库中符合条件的总记录数。 */
    private int totalCount;

    /** 总页数，由 totalCount 和 pageSize 计算。 */
    private int totalPages;

    /** 当前页的实际数据。 */
    private List<T> data;

    /** 保留搜索关键字，翻页时可继续使用。 */
    private String keywords;

    /** 无参构造器供框架或手动逐项赋值使用。 */
    public PageBean()
    {
    }

    /**
     * 创建一个完整分页对象。
     *
     * @param currentPage
     *            当前页码
     * @param pageSize
     *            每页条数
     * @param totalCount
     *            总记录数
     * @param data
     *            当前页数据
     */
    public PageBean(int currentPage, int pageSize, int totalCount, List<T> data)
    {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.data = data;
        // 先转成 double 才会进行小数除法，再向上取整：21 条、每页 10 条应得到 3 页。
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
    }

    // 标准 JavaBean getter/setter。
    public int getCurrentPage()
    {
        return currentPage;
    }

    public void setCurrentPage(int v)
    {
        currentPage = v;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int v)
    {
        pageSize = v;
    }

    public int getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(int v)
    {
        totalCount = v;
    }

    public int getTotalPages()
    {
        return totalPages;
    }

    public void setTotalPages(int v)
    {
        totalPages = v;
    }

    public List<T> getData()
    {
        return data;
    }

    public void setData(List<T> v)
    {
        data = v;
    }

    public String getKeywords()
    {
        return keywords;
    }

    public void setKeywords(String v)
    {
        keywords = v;
    }
}
