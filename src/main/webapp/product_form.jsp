<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid">
					<h1>${empty product?'新增产品':'编辑产品'}</h1>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid">
					<form method="post" action="${pageContext.request.contextPath}/product/save">
						<input type="hidden" name="id" value="${product.id}">
						<div class="card">
							<div class="card-body">
								<div class="row">
									<div class="form-group col-md-6">
										<label>产品名称 *</label>
										<input name="productName" value="${product.productName}" class="form-control" required>
									</div>
									<div class="form-group col-md-3">
										<label>分类</label>
										<select name="category" class="form-control">
											<c:forEach items="${['软件','硬件','服务','咨询']}" var="v">
												<option ${product.category==v?'selected':''}>${v}</option>
											</c:forEach>
										</select>
									</div>
									<div class="form-group col-md-3">
										<label>单位</label>
										<input name="unit" value="${empty product.unit?'套':product.unit}" class="form-control">
									</div>
									<div class="form-group col-md-4">
										<label>标准单价 *</label>
										<input name="unitPrice" type="number" min="0" step="0.01" value="${product.unitPrice}" class="form-control" required>
									</div>
									<div class="form-group col-12">
										<label>产品描述</label>
										<textarea name="description" class="form-control" rows="5"><c:out value="${product.description}" /></textarea>
									</div>
								</div>
							</div>
							<div class="card-footer text-right">
								<a href="${pageContext.request.contextPath}/product/list" class="btn btn-light">取消</a>
								<button class="btn btn-primary">
									<i class="fas fa-save"></i>
									保存产品
								</button>
							</div>
						</div>
					</form>
				</div>
			</section>
		</div><jsp:include page="footer.jsp" /></div><jsp:include page="scripts.jsp" /></body>
</html>
