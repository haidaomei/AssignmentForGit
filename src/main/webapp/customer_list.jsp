<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <jsp:include page="header.jsp" />
        </head>

        <body class="hold-transition sidebar-mini">
            <div class="wrapper">
                <jsp:include page="navbar.jsp" />
                <jsp:include page="sidebar.jsp" />
                <div class="content-wrapper">
                    <div class="content-header">
                        <div class="container-fluid d-flex justify-content-between">
                            <div>
                                <h1>${sessionScope.user.sales?'我的客户':'客户管理'}</h1>
                                <p class="text-muted mb-0">沉淀客户资料，跟踪最近联系状态</p>
                            </div><a href="${pageContext.request.contextPath}/customer/add"
                                class="btn btn-primary align-self-center"><i class="fas fa-plus mr-1"></i>新增客户</a>
                        </div>
                    </div>
                    <section class="content">
                        <div class="container-fluid">
                            <jsp:include page="flash.jsp" />
                            <div class="card">
                                <div class="card-header">
                                    <form class="form-inline" action="${pageContext.request.contextPath}/customer/list">
                                        <div class="input-group"><input name="keyword" value="${keyword}"
                                                class="form-control" placeholder="客户名称或编号">
                                            <div class="input-group-append"><button class="btn btn-primary"><i
                                                        class="fas fa-search"></i> 搜索</button></div>
                                        </div>
                                    </form>
                                </div>
                                <div class="card-body p-0">
                                    <div class="table-responsive">
                                        <table class="table mb-0">
                                            <thead>
                                                <tr>
                                                    <th>客户编号</th>
                                                    <th>客户名称</th>
                                                    <th>行业</th>
                                                    <th>等级</th>
                                                    <th>来源</th>
                                                    <th>负责人</th>
                                                    <th>最近跟进</th>
                                                    <th>操作</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${customerList}" var="x">
                                                    <tr class="${x.warningDays>30?'table-danger':''}">
                                                        <td><a
                                                                href="${pageContext.request.contextPath}/customer/detail?id=${x.id}">${x.customerNo}</a>
                                                        </td>
                                                        <td><strong>
                                                                <c:out value="${x.customerName}" />
                                                            </strong></td>
                                                        <td>
                                                            <c:out value="${x.industry}" />
                                                        </td>
                                                        <td><span
                                                                class="badge ${x.levelName=='VIP客户'?'badge-danger':x.levelName=='重点客户'?'badge-warning':x.levelName=='普通客户'?'badge-info':'badge-secondary'}">${x.levelName}</span>
                                                        </td>
                                                        <td>${x.sourceName}</td>
                                                        <td>${x.ownerName}</td>
                                                        <td>${empty x.lastFollowTime?'从未跟进':x.lastFollowTime}<c:if
                                                                test="${x.warningDays>30}"><i
                                                                    class="fas fa-exclamation-circle ml-1"></i></c:if>
                                                        </td>
                                                        <td class="text-nowrap"><a
                                                                class="btn btn-xs btn-outline-primary"
                                                                href="${pageContext.request.contextPath}/customer/detail?id=${x.id}">详情</a>
                                                            <a class="btn btn-xs btn-outline-secondary"
                                                                href="${pageContext.request.contextPath}/customer/edit?id=${x.id}">编辑</a>
                                                            <form class="d-inline" method="post"
                                                                action="${pageContext.request.contextPath}/customer/delete"
                                                                onsubmit="return confirm('确定删除该客户？相关业务数据将一并隐藏。')"><input
                                                                    type="hidden" name="id" value="${x.id}"><button
                                                                    class="btn btn-xs btn-danger">删除</button></form>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty customerList}">
                                                    <tr>
                                                        <td colspan="8">
                                                            <div class="empty-state"><i class="far fa-folder-open"></i>
                                                                <div>暂无客户数据</div>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                                <div class="card-footer">
                                    <jsp:include page="pagination.jsp" />
                                </div>
                            </div>
                        </div>
                    </section>
                </div>
                <jsp:include page="footer.jsp" />
            </div>
            <jsp:include page="scripts.jsp" />
        </body>

        </html>