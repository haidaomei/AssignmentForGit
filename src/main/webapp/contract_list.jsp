<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid d-flex justify-content-between">
					<div>
						<h1>合同管理</h1>
						<p class="text-muted mb-0">管理已成交商机的合同执行情况</p>
					</div>
					<a href="${pageContext.request.contextPath}/contract/add" class="btn btn-primary align-self-center">
						<i class="fas fa-plus"></i>
						新增合同
					</a>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid"><jsp:include page="flash.jsp" /><div class="card">
						<div class="card-header">
							<form class="form-inline" action="${pageContext.request.contextPath}/contract/list" method="get">
								<input name="keyword" value="${keyword}" class="form-control mr-2" placeholder="合同编号、名称或客户">
								<button class="btn btn-primary mr-2">
									<i class="fas fa-search"></i>
									搜索
								</button>
								<a class="btn btn-light" href="${pageContext.request.contextPath}/contract/list">重置</a>
							</form>
						</div>
						<div class="card-body p-0">
							<div class="table-responsive">
								<table class="table mb-0">
									<thead>
										<tr>
											<th>合同编号</th>
											<th>合同名称</th>
											<th>客户</th>
											<th>合同金额</th>
											<th>签订日期</th>
											<th>到期日期</th>
											<th>状态</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${contractList}" var="x">
											<tr>
												<td>${x.contractNo}</td>
												<td>
													<a href="${pageContext.request.contextPath}/contract/detail?id=${x.id}">
														<strong>
															<c:out value="${x.contractName}" />
														</strong>
													</a>
												</td>
												<td>${x.customerName}</td>
												<td class="money">
													¥
													<fmt:formatNumber value="${x.contractAmount}" pattern="#,#00.00" />
												</td>
												<td>${x.signedDate}</td>
												<td>${x.endDate}</td>
												<td>
													<span class="badge ${x.businessStatus=='执行中'?'badge-success':x.businessStatus=='已到期'?'badge-warning':'badge-secondary'}">${x.businessStatus}</span>
												</td>
												<td>
													<a class="btn btn-xs btn-outline-primary" href="${pageContext.request.contextPath}/contract/detail?id=${x.id}">详情</a>
													<a class="btn btn-xs btn-outline-secondary" href="${pageContext.request.contextPath}/contract/edit?id=${x.id}">编辑</a>
													<form class="d-inline" method="post" action="${pageContext.request.contextPath}/contract/delete" onsubmit="return confirm('确定删除？')">
														<input type="hidden" name="id" value="${x.id}">
														<button class="btn btn-xs btn-danger">删除</button>
													</form>
												</td>
											</tr>
										</c:forEach>
										<c:if test="${empty contractList}">
											<tr>
												<td colspan="8">
													<div class="empty-state">
														<i class="fas fa-file-contract"></i>
														<div>暂无合同</div>
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
