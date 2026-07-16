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
                            <h1>${empty contact?'新增联系人':'编辑联系人'}</h1>
                        </div>
                    </div>
                    <section class="content">
                        <div class="container-fluid">
                            <form method="post" action="${pageContext.request.contextPath}/contact/save"><input
                                    type="hidden" name="id" value="${contact.id}">
                                <div class="card">
                                    <div class="card-header">
                                        <h3 class="card-title">联系人资料</h3>
                                    </div>
                                    <div class="card-body">
                                        <div class="row">
                                            <div class="form-group col-md-6"><label>所属客户 *</label><select
                                                    name="customerId" class="form-control" required>
                                                    <option value="">请选择</option>
                                                    <c:forEach items="${customerList}" var="c">
                                                        <option value="${c.id}" ${contact.customerId==c.id||(empty
                                                            contact&&selectedCustomerId==c.id)?'selected':''}>
                                                            ${c.customerName}</option>
                                                    </c:forEach>
                                                </select></div>
                                            <div class="form-group col-md-3"><label>姓名 *</label><input name="name"
                                                    value="${contact.name}" class="form-control" required></div>
                                            <div class="form-group col-md-3"><label>性别</label><select name="gender"
                                                    class="form-control">
                                                    <option value="">未指定</option>
                                                    <option ${contact.gender=='男' ?'selected':''}>男</option>
                                                    <option ${contact.gender=='女' ?'selected':''}>女</option>
                                                </select></div>
                                            <div class="form-group col-md-4"><label>职务</label><input name="position"
                                                    value="${contact.position}" class="form-control"></div>
                                            <div class="form-group col-md-4"><label>手机</label><input name="phone"
                                                    value="${contact.phone}" class="form-control"></div>
                                            <div class="form-group col-md-4"><label>邮箱</label><input name="email"
                                                    value="${contact.email}" type="email" class="form-control"></div>
                                            <div class="form-group col-md-4"><label>微信</label><input name="wechat"
                                                    value="${contact.wechat}" class="form-control"></div>
                                            <div class="form-group col-md-4 pt-md-4"><label class="mr-3"><input
                                                        type="checkbox" name="isPrimary" value="1"
                                                        ${contact.isPrimary==1?'checked':''}> 主联系人</label><label><input
                                                        type="checkbox" name="isDecisionMaker" value="1"
                                                        ${contact.isDecisionMaker==1?'checked':''}> 决策人</label></div>
                                            <div class="form-group col-md-4"><label>兴趣爱好</label><input name="hobby"
                                                    value="${contact.hobby}" class="form-control"></div>
                                            <div class="form-group col-12"><label>备注</label><textarea name="remarks"
                                                    class="form-control"
                                                    rows="4"><c:out value="${contact.remarks}"/></textarea></div>
                                        </div>
                                    </div>
                                    <div class="card-footer text-right"><a
                                            href="${pageContext.request.contextPath}/contact/list"
                                            class="btn btn-light">取消</a> <button class="btn btn-primary"><i
                                                class="fas fa-save"></i> 保存联系人</button></div>
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