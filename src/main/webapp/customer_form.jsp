<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                        <div class="container-fluid">
                            <h1>${empty customer?'新增客户':'编辑客户'}</h1>
                        </div>
                    </div>
                    <section class="content">
                        <div class="container-fluid">
                            <form action="${pageContext.request.contextPath}/customer/save" method="post"><input
                                    type="hidden" name="id" value="${customer.id}">
                                <div class="card">
                                    <div class="card-header">
                                        <h3 class="card-title">客户基本资料</h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="row">
                                            <div class="form-group col-md-6"><label>客户名称 *</label><input
                                                    name="customerName" class="form-control" required maxlength="100"
                                                    value="${customer.customerName}"></div>
                                            <div class="form-group col-md-3"><label>行业</label><input name="industry"
                                                    class="form-control" value="${customer.industry}"></div>
                                            <div class="form-group col-md-3"><label>企业规模</label><select name="scale"
                                                    class="form-control">
                                                    <option value="">请选择</option>
                                                    <c:forEach
                                                        items="${['50人以下','50-100人','100-500人','500-1000人','1000人以上']}"
                                                        var="v">
                                                        <option ${customer.scale==v?'selected':''}>${v}</option>
                                                    </c:forEach>
                                                </select></div>
                                            <div class="form-group col-md-3"><label>省份</label><input name="province"
                                                    class="form-control" value="${customer.province}"></div>
                                            <div class="form-group col-md-3"><label>城市</label><input name="city"
                                                    class="form-control" value="${customer.city}"></div>
                                            <div class="form-group col-md-6"><label>详细地址</label><input name="address"
                                                    class="form-control" value="${customer.address}"></div>
                                            <div class="form-group col-md-6"><label>官方网站</label><input name="website"
                                                    type="url" class="form-control" value="${customer.website}"
                                                    placeholder="https://"></div>
                                            <div class="form-group col-md-3"><label>客户等级</label><select name="levelId"
                                                    class="form-control">
                                                    <option value="">请选择</option>
                                                    <c:forEach items="${levelList}" var="v">
                                                        <option value="${v.id}" ${customer.levelId==v.id?'selected':''}>
                                                            ${v.name}</option>
                                                    </c:forEach>
                                                </select></div>
                                            <div class="form-group col-md-3"><label>线索来源</label><select name="sourceId"
                                                    class="form-control">
                                                    <option value="">请选择</option>
                                                    <c:forEach items="${sourceList}" var="v">
                                                        <option value="${v.id}"
                                                            ${customer.sourceId==v.id?'selected':''}>${v.name}</option>
                                                    </c:forEach>
                                                </select></div>
                                            <div class="form-group col-md-4"><label>负责人</label><select
                                                    name="ownerUserId" class="form-control"
                                                    ${sessionScope.user.sales?'disabled':''}>
                                                    <c:forEach items="${userList}" var="u">
                                                        <option value="${u.id}" ${(empty
                                                            customer&&u.id==sessionScope.user.id)||customer.ownerUserId==u.id?'selected':''}>
                                                            ${u.realName}（${u.roleName}）</option>
                                                    </c:forEach>
                                                </select>
                                                <c:if test="${sessionScope.user.sales}"><input type="hidden"
                                                        name="ownerUserId" value="${sessionScope.user.id}"></c:if>
                                            </div>
                                            <div class="form-group col-md-2"><label>信用评级</label><select
                                                    name="creditRating" class="form-control">
                                                    <c:forEach items="${['A','B','C']}" var="v">
                                                        <option ${customer.creditRating==v?'selected':''}>${v}</option>
                                                    </c:forEach>
                                                </select></div>
                                            <div class="form-group col-12"><label>客户描述</label><textarea
                                                    name="description" class="form-control"
                                                    rows="4"><c:out value="${customer.description}"/></textarea></div>
                                        </div>
                                    </div>
                                    <div class="card-footer text-right"><a
                                            href="${pageContext.request.contextPath}/customer/list"
                                            class="btn btn-light mr-2">取消</a><button class="btn btn-primary"><i
                                                class="fas fa-save mr-1"></i>保存客户</button></div>
                                </div>
                            </form>
                        </div>
                    </section>
                </div>
                <jsp:include page="footer.jsp" />
            </div>
            <jsp:include page="scripts.jsp" />
        </body>

        </html>