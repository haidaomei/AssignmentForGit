<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <jsp:include page="header.jsp" />
        </head>

        <body class="auth-bg">
            <div class="register-card">
                <div class="text-center mb-4">
                    <div class="logo-square">L</div>
                    <h2>创建销售账号</h2>
                    <p class="text-muted">注册后角色默认为销售员</p>
                </div>
                <c:if test="${not empty error}">
                    <div class="alert alert-danger">
                        <c:out value="${error}" />
                    </div>
                </c:if>
                <form action="${pageContext.request.contextPath}/register" method="post">
                    <div class="row">
                        <div class="form-group col-md-6"><label>用户名 *</label><input name="username" class="form-control"
                                required></div>
                        <div class="form-group col-md-6"><label>真实姓名 *</label><input name="realName"
                                class="form-control" required></div>
                        <div class="form-group col-md-6"><label>手机号</label><input name="phone" class="form-control">
                        </div>
                        <div class="form-group col-md-6"><label>邮箱</label><input name="email" type="email"
                                class="form-control"></div>
                        <div class="form-group col-md-6"><label>密码 *</label><input name="password" type="password"
                                minlength="6" class="form-control" required></div>
                        <div class="form-group col-md-6"><label>确认密码 *</label><input name="confirmPassword"
                                type="password" minlength="6" class="form-control" required></div>
                        <div class="form-group col-12"><label>验证码 *</label>
                            <div class="code-row"><input name="code" maxlength="4" class="form-control" required><img
                                    src="${pageContext.request.contextPath}/checkCodeServlet"
                                    onclick="this.src='${pageContext.request.contextPath}/checkCodeServlet?t='+Date.now()"
                                    alt="验证码"></div>
                        </div>
                    </div><button class="login-btn">注 册</button>
                    <div class="text-center mt-3"><a href="${pageContext.request.contextPath}/login.jsp">已有账号，返回登录</a>
                    </div>
                </form>
            </div>
        </body>

        </html>