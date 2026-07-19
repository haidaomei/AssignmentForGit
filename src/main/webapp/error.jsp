<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="error-page-custom">
	<div class="error-card">
		<div class="error-code">${pageContext.errorData.statusCode==0?'!':pageContext.errorData.statusCode}</div>
		<h2>页面暂时无法访问</h2>
		<p>
			<c:out value="${empty errorMessage?'请求的页面不存在，或系统暂时发生异常。':errorMessage}" />
		</p>
		<a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary">
			<i class="fas fa-home mr-1"></i>
			返回首页
		</a>
	</div>
</body>
</html>
