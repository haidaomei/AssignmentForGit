<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid">
					<h1>客户流失预警</h1>
					<p class="text-muted mb-0">超过 30 天未跟进或从未跟进的客户，按逾期天数降序排列</p>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid">
					<div class="alert alert-warning">
						<i class="fas fa-info-circle mr-2"></i>
						建议优先联系 VIP 与重点客户，并及时记录下一次跟进计划。
					</div>
					<div class="card">
						<div class="card-body p-0">
							<div class="table-responsive">
								<table class="table mb-0">
									<thead>
										<tr>
											<th>客户名称</th>
											<th>等级</th>
											<th>负责人</th>
											<th>最近跟进</th>
											<th>逾期天数</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${customerList}" var="x">
											<tr class="table-danger">
												<td>
													<a href="${pageContext.request.contextPath}/customer/detail?id=${x.id}">
														<strong>
															<c:out value="${x.customerName}" />
														</strong>
													</a>
												</td>
												<td>
													<span class="badge badge-danger">${x.levelName}</span>
												</td>
												<td>${x.ownerName}</td>
												<td>${empty x.lastFollowTime?'从未跟进':x.lastFollowTime}</td>
												<td>
													<span class="badge badge-danger">
														<c:choose>
															<c:when test="${x.warningDays==999}">从未跟进</c:when>
															<c:otherwise>${x.warningDays} 天</c:otherwise>
														</c:choose>
													</span>
												</td>
												<td>
													<a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/follow/add?customerId=${x.id}">
														<i class="fas fa-phone"></i>
														立即跟进
													</a>
												</td>
											</tr>
										</c:forEach>
										<c:if test="${empty customerList}">
											<tr>
												<td colspan="6">
													<div class="empty-state">
														<i class="fas fa-check-circle text-success"></i>
														<div>太棒了，当前没有流失预警客户</div>
													</div>
												</td>
											</tr>
										</c:if>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</section>
		</div><jsp:include page="footer.jsp" /></div><jsp:include page="scripts.jsp" /></body>
</html>
