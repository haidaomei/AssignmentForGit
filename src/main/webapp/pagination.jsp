<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${pageBean.totalPages>0}">
	<c:url var="previousUrl" value="">
		<c:param name="currentPage" value="${pageBean.currentPage-1}" />
		<c:param name="keyword" value="${keyword}" />
	</c:url>
	<c:url var="nextUrl" value="">
		<c:param name="currentPage" value="${pageBean.currentPage+1}" />
		<c:param name="keyword" value="${keyword}" />
	</c:url>
	<div class="d-flex justify-content-between align-items-center flex-wrap">
		<span class="text-muted">共 ${pageBean.totalCount} 条，第 ${pageBean.currentPage}/${pageBean.totalPages} 页</span>
		<ul class="pagination mb-0">
			<li class="page-item ${pageBean.currentPage<=1?'disabled':''}">
				<a class="page-link" href="${previousUrl}">上一页</a>
			</li>
			<c:forEach begin="1" end="${pageBean.totalPages}" var="p">
				<c:url var="numberUrl" value="">
					<c:param name="currentPage" value="${p}" />
					<c:param name="keyword" value="${keyword}" />
				</c:url>
				<li class="page-item ${p==pageBean.currentPage?'active':''}">
					<a class="page-link" href="${numberUrl}">${p}</a>
				</li>
			</c:forEach>
			<li class="page-item ${pageBean.currentPage>=pageBean.totalPages?'disabled':''}">
				<a class="page-link" href="${nextUrl}">下一页</a>
			</li>
		</ul>
	</div>
</c:if>
