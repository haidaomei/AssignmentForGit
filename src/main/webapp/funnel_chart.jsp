<%--
  企航CRM - 销售漏斗图页面

  功能说明：
  1. 以 ECharts 连续曲线漏斗展示各商机阶段的转化情况
  2. 支持"按商机数量"和"按预计金额"两种维度切换
  3. 无商机数据时显示友好提示，引导用户先创建商机

  数据来源：FunnelServlet → DashboardService.funnel() → DashboardDao.funnel()
  后台将 List<FunnelData> 通过 Gson 序列化为 JSON，存入 request 的 funnelJson 属性。
  每项数据包含 name(阶段名称)、value(商机数量)、amount(预计金额)。

  依赖：ECharts 5.4 CDN + /static/funnel-chart.js（自定义系列渲染器）
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<%-- 引入公共头部：meta 标签 + AdminLTE CSS + FontAwesome + style.css --%>
<jsp:include page="header.jsp" />
</head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<%-- 顶部导航栏：侧边栏折叠按钮 + 首页链接 + 预警徽标 + 用户信息 --%>
		<jsp:include page="navbar.jsp" />
		<%-- 左侧菜单栏：数据仪表盘、客户、联系人、产品、商机、跟进、合同、漏斗、预警 --%>
		<jsp:include page="sidebar.jsp" />
		<%-- 页面主内容区 --%>
		<div class="content-wrapper">
			<%-- 面包屑 + 页面标题 --%>
			<div class="content-header">
				<div class="container-fluid">
					<div class="row mb-2">
						<div class="col-sm-6">
							<h1>销售漏斗</h1>
							<p class="text-muted mb-0">观察各销售阶段的商机数量与预期金额</p>
						</div>
						<div class="col-sm-6">
							<ol class="breadcrumb float-sm-right">
								<li class="breadcrumb-item">
									<a href="${pageContext.request.contextPath}/dashboard">首页</a>
								</li>
								<li class="breadcrumb-item active">销售漏斗</li>
							</ol>
						</div>
					</div>
				</div>
			</div>
			<%-- 页面正文 --%>
			<section class="content">
				<div class="container-fluid">
					<%-- ========== 空数据判断：纯 EL + JSTL，不引入额外 JavaScript ========== --%>
					<c:choose>
						<%-- 情况1：后台返回的漏斗数据为空数组，说明数据库中尚无商机 --%>
						<c:when test="${empty funnelJson or funnelJson == '[]'}">
							<div class="card">
								<div class="card-header">
									<h3 class="card-title">阶段转化分布</h3>
								</div>
								<div class="card-body">
									<%-- 空数据提示：垂直居中、带图标和引导文案 --%>
									<div class="funnel-empty">
										<i class="fas fa-filter" style="font-size: 42px; color: #c8d4e3; margin-bottom: 14px; display: block;"></i>
										<div style="font-size: 15px; color: #728097;">暂无商机数据</div>
										<div style="font-size: 13px; color: #b0bfd1; margin-top: 6px;">请先创建商机，系统将自动汇总各阶段分布</div>
									</div>
								</div>
							</div>
						</c:when>
						<%-- 情况2：有商机数据，正常渲染漏斗图 --%>
						<c:otherwise>
							<div class="card">
								<div class="card-header">
									<h3 class="card-title">阶段转化分布</h3>
								</div>
								<div class="card-body">
									<%-- ECharts 漏斗图容器 --%>
									<div id="funnel" class="funnel-container"></div>
								</div>
							</div>
						</c:otherwise>
					</c:choose>
				</div>
			</section>
		</div>
		<%-- 底部版权信息 --%>
		<jsp:include page="footer.jsp" />
	</div>
	<%-- 公共脚本：jQuery → Bootstrap → AdminLTE --%>
	<jsp:include page="scripts.jsp" />
	<%-- ECharts 5.4 CDN，优先使用 jsdelivr，加载失败时切换到 bootcdn 作为备用 --%>
	<script src="https://cdn.jsdelivr.net/npm/echarts@5.4/dist/echarts.min.js"></script>
	<script>
		if (typeof echarts === 'undefined') {
			document
					.write('<script src="https://cdn.bootcdn.net/ajax/libs/echarts/5.4.3/echarts.min.js"><\/script>');
		}
	</script>
	<%-- 企航CRM 漏斗图渲染器（基于 ECharts 5 内置漏斗系列） --%>
	<script src="${pageContext.request.contextPath}/static/funnel-chart.js"></script>
	<%-- 漏斗初始化：后台 Gson JSON 直接嵌入为 JS 对象字面量 --%>
	<c:if test="${not empty funnelJson and funnelJson != '[]'}">
		<script>
			(function() {
				var data = $
				{
					funnelJson
				}
				;
				QihangFunnel.create(document.getElementById("funnel"), data, {
					metric : "value"
				});
			})();
		</script>
	</c:if>
</body>
</html>
