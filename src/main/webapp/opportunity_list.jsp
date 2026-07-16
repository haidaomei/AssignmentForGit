<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
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
						<h1>${sessionScope.user.sales?'我的商机':'商机管理'}</h1>
						<p class="text-muted mb-0">管理从线索到成交的完整销售过程</p>
					</div>
					<a href="${pageContext.request.contextPath}/opportunity/add" class="btn btn-primary align-self-center">
						<i class="fas fa-plus"></i>
						新增商机
					</a>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid">
					<jsp:include page="flash.jsp" />
					<div class="card">
						<div class="card-header">
							<form class="form-inline">
								<select name="stageId" class="form-control mr-2">
									<option value="">全部阶段</option>
									<c:forEach items="${stageList}" var="s">
										<option value="${s.id}" ${param.stageId==s.id?'selected':''}>${s.name}</option>
									</c:forEach>
								</select>
								<select name="businessStatus" class="form-control mr-2">
									<option value="">全部状态</option>
									<c:forEach items="${['进行中','已成交','已丢单']}" var="v">
										<option ${param.businessStatus==v?'selected':''}>${v}</option>
									</c:forEach>
								</select>
								<button class="btn btn-primary">
									<i class="fas fa-filter"></i>
									筛选
								</button>
							</form>
						</div>
						<div class="card-body p-0">
							<div class="table-responsive">
								<table class="table mb-0">
									<thead>
										<tr>
											<th>商机编号</th>
											<th>商机标题</th>
											<th>客户</th>
											<th>当前阶段</th>
											<th>预计金额</th>
											<th>预计成交</th>
											<th>负责人</th>
											<th>状态</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${opportunityList}" var="x">
											<tr>
												<td>${x.opportunityNo}</td>
												<td>
													<a href="${pageContext.request.contextPath}/opportunity/detail?id=${x.id}">
														<strong>
															<c:out value="${x.title}" />
														</strong>
													</a>
												</td>
												<td>
													<c:out value="${x.customerName}" />
												</td>
												<td>
													<span class="badge ${x.stageName=='成交'?'badge-success':x.stageName=='丢单'?'badge-danger':'badge-info'}">${x.stageName} · ${x.probability}%</span>
												</td>
												<td class="money">
													¥
													<fmt:formatNumber value="${x.expectedAmount}" pattern="#,#00.00" />
												</td>
												<td>${x.estimatedCloseDate}</td>
												<td>${x.ownerName}</td>
												<td>
													<span class="badge ${x.businessStatus=='已成交'?'badge-success':x.businessStatus=='已丢单'?'badge-danger':'badge-warning'}">${x.businessStatus}</span>
												</td>
												<td class="text-nowrap">
													<a class="btn btn-xs btn-outline-primary" href="${pageContext.request.contextPath}/opportunity/detail?id=${x.id}">详情</a>
													<a class="btn btn-xs btn-outline-secondary" href="${pageContext.request.contextPath}/opportunity/edit?id=${x.id}">编辑</a>
													<form class="d-inline" method="post" action="${pageContext.request.contextPath}/opportunity/delete" onsubmit="return confirm('确定删除该商机？')">
														<input type="hidden" name="id" value="${x.id}">
														<button class="btn btn-xs btn-danger">删除</button>
													</form>
												</td>
											</tr>
										</c:forEach>
										<c:if test="${empty opportunityList}">
											<tr>
												<td colspan="9">
													<div class="empty-state">
														<i class="fas fa-chart-line"></i>
														<div>暂无符合条件的商机</div>
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