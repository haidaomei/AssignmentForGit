<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid d-flex justify-content-between">
					<div>
						<h1>
							<c:out value="${opportunity.title}" />
						</h1>
						<p class="text-muted mb-0">${opportunity.opportunityNo}</p>
					</div>
					<div>
						<a href="${pageContext.request.contextPath}/opportunity/edit?id=${opportunity.id}" class="btn btn-outline-primary">编辑</a>
						<c:if test="${opportunity.businessStatus=='已成交'&&!hasContract}">
							<a href="${pageContext.request.contextPath}/contract/add?opportunityId=${opportunity.id}" class="btn btn-success">
								<i class="fas fa-file-contract"></i>
								生成合同
							</a>
						</c:if>
					</div>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid"><jsp:include page="flash.jsp" /><div class="row">
						<div class="col-lg-8">
							<div class="card">
								<div class="card-header">
									<h3 class="card-title">商机信息</h3>
								</div>
								<div class="card-body">
									<div class="row">
										<div class="col-md-4">
											<div class="detail-label">客户</div>
											<div class="detail-value">
												<a href="${pageContext.request.contextPath}/customer/detail?id=${opportunity.customerId}">${opportunity.customerName}</a>
											</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">主要联系人</div>
											<div class="detail-value">${opportunity.contactName}</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">负责人</div>
											<div class="detail-value">${opportunity.ownerName}</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">当前阶段</div>
											<div class="detail-value">
												<span class="badge badge-info">${opportunity.stageName} · ${opportunity.probability}%</span>
											</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">预计金额</div>
											<div class="detail-value money">
												¥
												<fmt:formatNumber value="${opportunity.expectedAmount}" pattern="#,#00.00" />
											</div>
										</div>
										<div class="col-md-4">
											<div class="detail-label">预计成交</div>
											<div class="detail-value">${opportunity.estimatedCloseDate}</div>
										</div>
										<div class="col-12">
											<div class="detail-label">商机描述</div>
											<div>
												<c:out value="${opportunity.description}" />
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="col-lg-4">
							<div class="card">
								<div class="card-header">
									<h3 class="card-title">阶段推进</h3>
								</div>
								<div class="card-body">
									<div class="mb-3">
										<span class="badge ${opportunity.businessStatus=='已成交'?'badge-success':opportunity.businessStatus=='已丢单'?'badge-danger':'badge-warning'}">${opportunity.businessStatus}</span>
									</div>
									<c:if test="${opportunity.businessStatus=='进行中'}">
										<form method="post" action="${pageContext.request.contextPath}/opportunity/advance" onsubmit="return checkReason(this)">
											<input type="hidden" name="id" value="${opportunity.id}">
											<div class="form-group">
												<select name="stageId" class="form-control" id="nextStage" required>
													<c:forEach items="${stageList}" var="s">
														<c:if test="${s.sortOrder>opportunity.stageSort}">
															<option value="${s.id}" data-name="${s.name}">${s.name}（${s.probability}%）</option>
														</c:if>
													</c:forEach>
												</select>
											</div>
											<input type="hidden" name="reason" id="reason">
											<button class="btn btn-primary btn-block">
												<i class="fas fa-arrow-right"></i>
												推进阶段
											</button>
										</form>
									</c:if>
									<c:if test="${opportunity.businessStatus!='进行中'}">
										<p class="text-muted mb-0">该商机已结束。如需继续，请新建商机。</p>
									</c:if>
								</div>
							</div>
						</div>
					</div>
					<div class="card">
						<div class="card-header">
							<h3 class="card-title">产品明细</h3>
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
									<c:forEach items="${opportunity.items}" var="x">
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
					<div class="card">
						<div class="card-header">
							<h3 class="card-title">跟进记录</h3>
							<a href="${pageContext.request.contextPath}/follow/add?customerId=${opportunity.customerId}&opportunityId=${opportunity.id}" class="float-right">新增跟进</a>
						</div>
						<div class="card-body">
							<div class="timeline-simple">
								<c:forEach items="${followList}" var="x">
									<div class="timeline-item-simple">
										<div class="timeline-time">${x.followTime}· ${x.followUserName}</div>
										<strong>
											<c:out value="${x.followContent}" />
										</strong>
									</div>
								</c:forEach>
								<c:if test="${empty followList}">
									<span class="text-muted">暂无跟进记录</span>
								</c:if>
							</div>
						</div>
					</div>
				</div>
			</section>
		</div><jsp:include page="footer.jsp" /></div><jsp:include page="scripts.jsp" /><script>
			function checkReason(f) {
				const o = document.getElementById('nextStage').selectedOptions[0];
				if (o && o.dataset.name === '丢单') {
					const r = prompt('请填写丢单原因：');
					if (!r)
						return false;
					document.getElementById('reason').value = r
				}
				return confirm('确定推进到“' + o.dataset.name + '”？系统将自动记录跟进。')
			}
		</script>
</body>
</html>
