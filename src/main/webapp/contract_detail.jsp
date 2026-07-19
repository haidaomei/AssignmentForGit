<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid d-flex justify-content-between">
					<div>
						<h1>
							<c:out value="${contract.contractName}" />
						</h1>
						<p class="text-muted mb-0">${contract.contractNo}</p>
					</div>
					<a href="${pageContext.request.contextPath}/contract/edit?id=${contract.id}" class="btn btn-primary">
						<i class="fas fa-edit"></i>
						编辑合同
					</a>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid">
					<div class="row">
						<div class="col-lg-8">
							<div class="card">
								<div class="card-header">
									<h3 class="card-title">合同信息</h3>
								</div>
								<div class="card-body">
									<div class="row">
										<div class="col-md-4">
											<div class="detail-label">客户</div>
											<div class="detail-value">
												<a href="${pageContext.request.contextPath}/customer/detail?id=${contract.customerId}">${contract.customerName}</a>
											</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">合同金额</div>
											<div class="detail-value money text-primary">
												¥
												<fmt:formatNumber value="${contract.contractAmount}" pattern="#,#00.00" />
											</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">状态</div>
											<div class="detail-value">
												<span class="badge badge-success">${contract.businessStatus}</span>
											</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">签订日期</div>
											<div class="detail-value">${contract.signedDate}</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">执行期间</div>
											<div class="detail-value">${contract.startDate}至 ${contract.endDate}</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">创建人</div>
											<div class="detail-value">${contract.createUserName}</div>
										</div>
										<div class="col-12">
											<div class="detail-label">付款条款</div>
											<div class="detail-value">
												<c:out value="${contract.paymentTerms}" />
											</div>
										</div>
										<div class="col-12">
											<div class="detail-label">备注</div>
											<div>
												<c:out value="${contract.remarks}" />
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="col-lg-4">
							<div class="card">
								<div class="card-header">
									<h3 class="card-title">关联商机</h3>
								</div>
								<div class="card-body">
									<c:choose>
										<c:when test="${not empty contract.opportunityId}">
											<a href="${pageContext.request.contextPath}/opportunity/detail?id=${contract.opportunityId}">
												<i class="fas fa-chart-line mr-2"></i>
												<c:out value="${contract.opportunityTitle}" />
											</a>
										</c:when>
										<c:otherwise>
											<span class="text-muted">未关联商机</span>
										</c:otherwise>
									</c:choose>
								</div>
							</div>
						</div>
					</div>
					<div class="card">
						<div class="card-header">
							<h3 class="card-title">合同产品明细</h3>
						</div>
						<div class="card-body p-0">
							<table class="table mb-0">
								<thead>
									<tr>
										<th>产品</th>
										<th>数量</th>
										<th>单价</th>
										<th>小计</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach items="${contract.items}" var="x">
										<tr>
											<td>${x.productName}</td>
											<td>${x.quantity}</td>
											<td>
												¥
												<fmt:formatNumber value="${x.unitPrice}" pattern="#,#00.00" />
											</td>
											<td class="money">
												¥
												<fmt:formatNumber value="${x.subtotal}" pattern="#,#00.00" />
											</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</section>
		</div><jsp:include page="footer.jsp" /></div><jsp:include page="scripts.jsp" /></body>
</html>
