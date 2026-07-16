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
                                <h1>联系人管理</h1>
                                <p class="text-muted mb-0">维护客户关键联系人与决策链</p>
                            </div><a href="${pageContext.request.contextPath}/contact/add"
                                class="btn btn-primary align-self-center"><i class="fas fa-plus"></i> 新增联系人</a>
                        </div>
                    </div>
                    <section class="content">
                        <div class="container-fluid">
                            <jsp:include page="flash.jsp" />
                            <div class="card">
                                <div class="card-header">
                                    <form class="form-inline"><input name="keyword" value="${keyword}"
                                            class="form-control mr-2" placeholder="姓名或手机号"><button
                                            class="btn btn-primary"><i class="fas fa-search"></i> 搜索</button></form>
                                </div>
                                <div class="card-body p-0">
                                    <div class="table-responsive">
                                        <table class="table mb-0">
                                            <thead>
                                                <tr>
                                                    <th>姓名</th>
                                                    <th>所属客户</th>
                                                    <th>职务</th>
                                                    <th>电话</th>
                                                    <th>邮箱</th>
                                                    <th>标签</th>
                                                    <th>操作</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${contactList}" var="x">
                                                    <tr>
                                                        <td><strong>
                                                                <c:out value="${x.name}" />
                                                            </strong></td>
                                                        <td><a
                                                                href="${pageContext.request.contextPath}/customer/detail?id=${x.customerId}">
                                                                <c:out value="${x.customerName}" />
                                                            </a></td>
                                                        <td>${x.position}</td>
                                                        <td>${x.phone}</td>
                                                        <td>${x.email}</td>
                                                        <td>
                                                            <c:if test="${x.isPrimary==1}"><span
                                                                    class="badge badge-info">主联系人</span></c:if>
                                                            <c:if test="${x.isDecisionMaker==1}"><span
                                                                    class="badge badge-warning">决策人</span></c:if>
                                                        </td>
                                                        <td><a class="btn btn-xs btn-outline-primary"
                                                                href="${pageContext.request.contextPath}/contact/edit?id=${x.id}">编辑</a>
                                                            <form class="d-inline" method="post"
                                                                action="${pageContext.request.contextPath}/contact/delete"
                                                                onsubmit="return confirm('确定删除？')"><input type="hidden"
                                                                    name="id" value="${x.id}"><button
                                                                    class="btn btn-xs btn-danger">删除</button></form>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                                <c:if test="${empty contactList}">
                                                    <tr>
                                                        <td colspan="7">
                                                            <div class="empty-state"><i class="far fa-address-book"></i>
                                                                <div>暂无联系人</div>
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