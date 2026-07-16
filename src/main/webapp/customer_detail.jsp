<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
                                    <h1>
                                        <c:out value="${customer.customerName}" />
                                    </h1>
                                    <p class="text-muted mb-0">${customer.customerNo}</p>
                                </div>
                                <div><a href="${pageContext.request.contextPath}/customer/edit?id=${customer.id}"
                                        class="btn btn-outline-primary"><i class="fas fa-edit"></i> 编辑</a> <a
                                        href="${pageContext.request.contextPath}/follow/add?customerId=${customer.id}"
                                        class="btn btn-primary"><i class="fas fa-phone"></i> 立即跟进</a></div>
                            </div>
                        </div>
                        <section class="content">
                            <div class="container-fluid">
                                <jsp:include page="flash.jsp" />
                                <div class="row">
                                    <div class="col-lg-8">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title">客户基本信息</h3>
                                            </div>
                                            <div class="card-body">
                                                <div class="row">
                                                    <div class="col-md-3">
                                                        <div class="detail-label">客户等级</div>
                                                        <div class="detail-value"><span
                                                                class="badge badge-info">${customer.levelName}</span>
                                                        </div>
                                                    </div>
                                                    <div class="col-md-3">
                                                        <div class="detail-label">行业 / 规模</div>
                                                        <div class="detail-value">${customer.industry} /
                                                            ${customer.scale}</div>
                                                    </div>
                                                    <div class="col-md-3">
                                                        <div class="detail-label">负责人</div>
                                                        <div class="detail-value">${customer.ownerName}</div>
                                                    </div>
                                                    <div class="col-md-3">
                                                        <div class="detail-label">信用评级</div>
                                                        <div class="detail-value">${customer.creditRating}</div>
                                                    </div>
                                                    <div class="col-md-6">
                                                        <div class="detail-label">地址</div>
                                                        <div class="detail-value">
                                                            ${customer.province}${customer.city}${customer.address}
                                                        </div>
                                                    </div>
                                                    <div class="col-md-3">
                                                        <div class="detail-label">来源</div>
                                                        <div class="detail-value">${customer.sourceName}</div>
                                                    </div>
                                                    <div class="col-md-3">
                                                        <div class="detail-label">最近跟进</div>
                                                        <div class="detail-value">${empty
                                                            customer.lastFollowTime?'从未跟进':customer.lastFollowTime}
                                                        </div>
                                                    </div>
                                                    <div class="col-12">
                                                        <div class="detail-label">客户描述</div>
                                                        <div>
                                                            <c:out value="${customer.description}" />
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-4">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title">客户归属</h3>
                                            </div>
                                            <div class="card-body">
                                                <div class="d-flex align-items-center mb-3"><span
                                                        class="user-avatar mr-3"><i class="fas fa-user"></i></span>
                                                    <div><strong>${customer.ownerName}</strong>
                                                        <div class="text-muted small">当前负责人</div>
                                                    </div>
                                                </div>
                                                <c:if test="${not sessionScope.user.sales}"><button
                                                        class="btn btn-outline-primary btn-block" data-toggle="modal"
                                                        data-target="#transferModal"><i class="fas fa-exchange-alt"></i>
                                                        转移客户</button></c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-lg-6">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title">联系人</h3><a
                                                    href="${pageContext.request.contextPath}/contact/add?customerId=${customer.id}"
                                                    class="float-right"><i class="fas fa-plus"></i> 新增</a>
                                            </div>
                                            <div class="card-body p-0">
                                                <table class="table mb-0">
                                                    <thead>
                                                        <tr>
                                                            <th>姓名</th>
                                                            <th>职务</th>
                                                            <th>电话</th>
                                                            <th>标签</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach items="${contactList}" var="x">
                                                            <tr>
                                                                <td><strong>${x.name}</strong></td>
                                                                <td>${x.position}</td>
                                                                <td>${x.phone}</td>
                                                                <td>
                                                                    <c:if test="${x.isPrimary==1}"><span
                                                                            class="badge badge-info">主联系人</span></c:if>
                                                                    <c:if test="${x.isDecisionMaker==1}"><span
                                                                            class="badge badge-warning">决策人</span>
                                                                    </c:if>
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                        <c:if test="${empty contactList}">
                                                            <tr>
                                                                <td colspan="4" class="text-center text-muted py-4">
                                                                    暂无联系人</td>
                                                            </tr>
                                                        </c:if>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-6">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title">关联商机</h3><a
                                                    href="${pageContext.request.contextPath}/opportunity/add?customerId=${customer.id}"
                                                    class="float-right"><i class="fas fa-plus"></i> 新增</a>
                                            </div>
                                            <div class="card-body p-0">
                                                <table class="table mb-0">
                                                    <thead>
                                                        <tr>
                                                            <th>商机</th>
                                                            <th>阶段</th>
                                                            <th>金额</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach items="${opportunityList}" var="x">
                                                            <tr>
                                                                <td><a
                                                                        href="${pageContext.request.contextPath}/opportunity/detail?id=${x.id}">${x.title}</a>
                                                                </td>
                                                                <td><span class="badge badge-info">${x.stageName}</span>
                                                                </td>
                                                                <td>¥
                                                                    <fmt:formatNumber value="${x.expectedAmount}"
                                                                        pattern="#,#00.00" />
                                                                </td>
                                                            </tr>
                                                        </c:forEach>
                                                        <c:if test="${empty opportunityList}">
                                                            <tr>
                                                                <td colspan="3" class="text-center text-muted py-4">暂无商机
                                                                </td>
                                                            </tr>
                                                        </c:if>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-lg-8">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title">跟进时间线</h3>
                                            </div>
                                            <div class="card-body">
                                                <div class="timeline-simple">
                                                    <c:forEach items="${followList}" var="x">
                                                        <div class="timeline-item-simple">
                                                            <div class="timeline-time">${x.followTime} ·
                                                                ${x.followUserName} · ${x.followType}</div><strong>
                                                                <c:out value="${x.followContent}" />
                                                            </strong>
                                                            <c:if test="${not empty x.customerFeedback}">
                                                                <div class="text-muted mt-1">客户反馈：
                                                                    <c:out value="${x.customerFeedback}" />
                                                                </div>
                                                            </c:if>
                                                        </div>
                                                    </c:forEach>
                                                    <c:if test="${empty followList}">
                                                        <div class="text-muted">暂无跟进记录</div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-4">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title">转移记录</h3>
                                            </div>
                                            <div class="card-body">
                                                <c:forEach items="${transferList}" var="x">
                                                    <div class="mb-3"><small
                                                            class="text-muted">${x.transferTime}</small>
                                                        <div>${empty x.fromUserName?'未分配':x.fromUserName} <i
                                                                class="fas fa-arrow-right text-primary mx-1"></i>
                                                            ${x.toUserName}</div><small>
                                                            <c:out value="${x.reason}" />
                                                        </small>
                                                    </div>
                                                </c:forEach>
                                                <c:if test="${empty transferList}"><span
                                                        class="text-muted">暂无转移记录</span></c:if>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <jsp:include page="footer.jsp" />
                </div>
                <c:if test="${not sessionScope.user.sales}">
                    <div class="modal fade" id="transferModal">
                        <div class="modal-dialog">
                            <form class="modal-content" action="${pageContext.request.contextPath}/customer/transfer"
                                method="post"><input type="hidden" name="id" value="${customer.id}">
                                <div class="modal-header">
                                    <h5 class="modal-title">转移客户</h5><button type="button" class="close"
                                        data-dismiss="modal">&times;</button>
                                </div>
                                <div class="modal-body">
                                    <div class="form-group"><label>新负责人 *</label><select name="toUserId"
                                            class="form-control" required>
                                            <c:forEach items="${userList}" var="u">
                                                <option value="${u.id}">${u.realName}（${u.roleName}）</option>
                                            </c:forEach>
                                        </select></div>
                                    <div class="form-group"><label>转移原因</label><textarea name="reason"
                                            class="form-control" rows="3" required></textarea></div>
                                </div>
                                <div class="modal-footer"><button type="button" class="btn btn-light"
                                        data-dismiss="modal">取消</button><button class="btn btn-primary">确认转移</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </c:if>
                <jsp:include page="scripts.jsp" />
            </body>

            </html>