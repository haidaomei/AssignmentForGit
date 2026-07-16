<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <c:if test="${pageBean.totalPages>0}">
            <div class="d-flex justify-content-between align-items-center flex-wrap"><span class="text-muted">共
                    ${pageBean.totalCount} 条，第 ${pageBean.currentPage}/${pageBean.totalPages} 页</span>
                <ul class="pagination mb-0">
                    <li class="page-item ${pageBean.currentPage<=1?'disabled':''}"><a class="page-link"
                            href="?currentPage=${pageBean.currentPage-1}&keyword=${param.keyword}&stageId=${param.stageId}&businessStatus=${param.businessStatus}&followType=${param.followType}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}">上一页</a>
                    </li>
                    <c:forEach begin="1" end="${pageBean.totalPages}" var="p">
                        <li class="page-item ${p==pageBean.currentPage?'active':''}"><a class="page-link"
                                href="?currentPage=${p}&keyword=${param.keyword}&stageId=${param.stageId}&businessStatus=${param.businessStatus}&followType=${param.followType}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}">${p}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${pageBean.currentPage>=pageBean.totalPages?'disabled':''}"><a
                            class="page-link"
                            href="?currentPage=${pageBean.currentPage+1}&keyword=${param.keyword}&stageId=${param.stageId}&businessStatus=${param.businessStatus}&followType=${param.followType}&dateFrom=${param.dateFrom}&dateTo=${param.dateTo}">下一页</a>
                    </li>
                </ul>
            </div>
        </c:if>