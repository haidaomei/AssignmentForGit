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
						<h1>产品管理</h1>
						<p class="text-muted mb-0">维护销售可选的产品和服务目录</p>
					</div>
					<a href="${pageContext.request.contextPath}/product/add" class="btn btn-primary align-self-center">
						<i class="fas fa-plus"></i>
						新增产品
					</a>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid">
					<jsp:include page="flash.jsp" />
					<div class="card">
						<div class="card-header">
							<form class="form-inline">
								<input name="keyword" value="${keyword}" class="form-control mr-2" placeholder="产品名称">
								<button class="btn btn-primary">
									<i class="fas fa-search"></i>
									搜索
								</button>
							</form>
						</div>
						<div class="card-body p-0">
							<table class="table mb-0">
								<thead>
									<tr>
										<th>产品名称</th>
										<th>分类</th>
										<th>单位</th>
										<th>标准单价</th>
										<th>状态</th>
										<th>操作</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${productList}" var="x">
										<tr>
											<td>
												<strong>
													<c:out value="${x.productName}" />
												</strong>
											</td>
											<td>
												<span class="badge ${x.category=='软件'?'badge-info':x.category=='硬件'?'badge-success':x.category=='服务'?'badge-purple':'badge-warning'}">${x.category}</span>
											</td>
											<td>${x.unit}</td>
											<td class="money">
												¥
												<fmt:formatNumber value="${x.unitPrice}" pattern="#,#00.00" />
											</td>
											<td>
												<span class="badge badge-success">上架</span>
											</td>
											<td>
												<a class="btn btn-xs btn-outline-primary" href="${pageContext.request.contextPath}/product/edit?id=${x.id}">编辑</a>
												<form class="d-inline" method="post" action="${pageContext.request.contextPath}/product/delete" onsubmit="return confirm('下架该产品？')">
													<input type="hidden" name="id" value="${x.id}">
													<button class="btn btn-xs btn-danger">下架</button>
												</form>
											</td>
										</tr>
									</c:forEach>
									<c:if test="${empty productList}">
										<tr>
											<td colspan="6">
												<div class="empty-state">
													<i class="fas fa-cubes"></i>
													<div>暂无产品</div>
												</div>
											</td>
										</tr>
									</c:if>
								</tbody>
							</table>
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