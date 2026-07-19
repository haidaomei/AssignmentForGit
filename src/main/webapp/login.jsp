<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body>
	<div class="login-page-custom">
		<section class="login-hero">
			<div class="hero-brand">
				<span>Q</span>
				企航CRM
			</div>
			<h1>
				让每一次跟进，
				<br>
				都更接近成交。
			</h1>
			<p>连接客户、联系人、商机与合同全过程，让销售团队以更清晰的数据做出更好的决策。</p>
			<div class="hero-points">
				<div class="hero-point">
					<strong>全链路</strong>
					<span>销售过程</span>
				</div>
				<div class="hero-point">
					<strong>实时</strong>
					<span>经营洞察</span>
				</div>
				<div class="hero-point">
					<strong>智能</strong>
					<span>跟进提醒</span>
				</div>
			</div>
		</section>
		<main class="login-panel">
			<form class="login-box" action="${pageContext.request.contextPath}/login" method="post">
				<div class="eyebrow">WELCOME BACK</div>
				<h2>欢迎回来</h2>
				<p>登录后查看最新销售进展</p>
				<div class="form-group">
					<label>用户名</label>
					<div class="input-icon">
						<i class="far fa-user"></i>
						<input name="username" class="form-control" required value="${rememberedUsername}" autocomplete="username">
					</div>
				</div>
				<div class="form-group">
					<label>密码</label>
					<div class="input-icon">
						<i class="fas fa-lock"></i>
						<input type="password" name="password" class="form-control" required value="${rememberedPassword}" autocomplete="current-password">
					</div>
				</div>
				<div class="form-group">
					<label>验证码</label>
					<div class="code-row">
						<input name="code" class="form-control" maxlength="4" required>
						<img src="${pageContext.request.contextPath}/checkCodeServlet" alt="验证码" onclick="this.src='${pageContext.request.contextPath}/checkCodeServlet?t='+Date.now()" title="点击刷新">
					</div>
				</div>
				<div class="d-flex justify-content-between align-items-center mb-3">
					<label class="mb-0">
						<input type="checkbox" name="remember" value="1" ${remembered?'checked':''}>
						记住我
					</label>
					<a href="${pageContext.request.contextPath}/register">注册账号</a>
				</div>
				<button class="login-btn">登 录</button>
			</form>
		</main>
	</div>
</body>
</html>
