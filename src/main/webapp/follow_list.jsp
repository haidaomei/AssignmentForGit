<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid d-flex justify-content-between">
					<div>
						<h1>跟进记录</h1>
						<p class="text-muted mb-0">记录每一次客户沟通和下一步计划</p>
					</div>
					<a href="${pageContext.request.contextPath}/follow/add" class="btn btn-primary align-self-center">
						<i class="fas fa-plus"></i>
						新增跟进
					</a>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid"><jsp:include page="flash.jsp" /><div class="card">
						<div class="card-header">
							<form class="form-inline" action="${pageContext.request.contextPath}/follow/list" method="get">
								<input name="keyword" value="${keyword}" class="form-control mr-2" placeholder="客户、商机、联系人或跟进内容">
								<button class="btn btn-primary mr-2">
									<i class="fas fa-search"></i>
									搜索
								</button>
								<a class="btn btn-light" href="${pageContext.request.contextPath}/follow/list">重置</a>
							</form>
						</div>
						<div class="card-body p-0">
							<div class="table-responsive">
								<table class="table mb-0">
									<thead>
										<tr>
											<th>跟进时间</th>
											<th>客户</th>
											<th>商机</th>
											<th>方式</th>
											<th>跟进内容</th>
											<th>联系人</th>
											<th>下次跟进</th>
											<th>跟进人</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${followList}" var="x">
											<tr class="${x.overdue?'table-warning':''}">
												<td>${x.followTime}</td>
												<td>
													<a href="${pageContext.request.contextPath}/customer/detail?id=${x.customerId}">${x.customerName}</a>
												</td>
												<td>${x.opportunityTitle}</td>
												<td>
													<span class="badge badge-info">${x.followType}</span>
												</td>
												<td>
													<c:out value="${x.followContent}" />
												</td>
												<td>${x.contactName}</td>
												<td>${x.nextFollowTime}<c:if test="${x.overdue}">
														<span class="badge badge-warning ml-1">逾期</span>
													</c:if>
												</td>
												<td>${x.followUserName}</td>
												<td>
													<form method="post" action="${pageContext.request.contextPath}/follow/delete" onsubmit="return confirm('确定删除？')">
														<input type="hidden" name="id" value="${x.id}">
														<button class="btn btn-xs btn-danger">删除</button>
													</form>
												</td>
											</tr>
										</c:forEach>
										<c:if test="${empty followList}">
											<tr>
												<td colspan="9">
													<div class="empty-state">
														<i class="fas fa-clipboard-list"></i>
														<div>暂无跟进记录</div>
													</div>
												</td>
											</tr>
										</c:if>
									</tbody>
								</table>
							</div>
						</div>
						<div class="card-footer"><jsp:include page="pagination.jsp" /></div>
					</div>
				</div>
			</section>
		</div><jsp:include page="footer.jsp" /></div><jsp:include page="scripts.jsp" /></body>
</html>
