<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="zh-CN">

            <head>
                <jsp:include page="header.jsp" />
            </head>

            <body class="hold-transition sidebar-mini layout-fixed">
                <div class="wrapper">
                    <jsp:include page="navbar.jsp" />
                    <jsp:include page="sidebar.jsp" />
                    <div class="content-wrapper">
                        <div class="content-header">
                            <div class="container-fluid">
                                <div class="d-flex justify-content-between align-items-center">
                                    <div>
                                        <h1>数据仪表盘</h1>
                                        <p class="text-muted mb-0">欢迎回来，${sessionScope.user.realName}。这里是今天的销售经营概览。</p>
                                    </div><a href="${pageContext.request.contextPath}/follow/add"
                                        class="btn btn-primary"><i class="fas fa-plus mr-1"></i>记录跟进</a>
                                </div>
                            </div>
                        </div>
                        <section class="content">
                            <div class="container-fluid">
                                <jsp:include page="flash.jsp" />
                                <div class="row">
                                    <div class="col-lg-2 col-md-4 col-6">
                                        <div class="small-box bg-info">
                                            <div class="inner">
                                                <h3>${stats.customerCount}</h3>
                                                <p>客户总数</p>
                                            </div>
                                            <div class="icon"><i class="fas fa-building"></i></div><a
                                                href="${pageContext.request.contextPath}/customer/list"
                                                class="small-box-footer">查看客户 <i
                                                    class="fas fa-arrow-circle-right"></i></a>
                                        </div>
                                    </div>
                                    <div class="col-lg-2 col-md-4 col-6">
                                        <div class="small-box bg-success">
                                            <div class="inner">
                                                <h3>${stats.monthCustomerCount}</h3>
                                                <p>本月新增</p>
                                            </div>
                                            <div class="icon"><i class="fas fa-user-plus"></i></div><span
                                                class="small-box-footer">持续拓展客户池</span>
                                        </div>
                                    </div>
                                    <div class="col-lg-2 col-md-4 col-6">
                                        <div class="small-box bg-warning">
                                            <div class="inner">
                                                <h3>${stats.activeOpportunityCount}</h3>
                                                <p>进行中商机</p>
                                            </div>
                                            <div class="icon"><i class="fas fa-chart-line"></i></div><a
                                                href="${pageContext.request.contextPath}/opportunity/list?businessStatus=进行中"
                                                class="small-box-footer">查看商机 <i
                                                    class="fas fa-arrow-circle-right"></i></a>
                                        </div>
                                    </div>
                                    <div class="col-lg-2 col-md-4 col-6">
                                        <div class="small-box bg-danger">
                                            <div class="inner">
                                                <h3><small>¥</small>
                                                    <fmt:formatNumber value="${stats.expectedAmount}" pattern="#,#00" />
                                                </h3>
                                                <p>预计成交金额</p>
                                            </div>
                                            <div class="icon"><i class="fas fa-yen-sign"></i></div><span
                                                class="small-box-footer">进行中商机总额</span>
                                        </div>
                                    </div>
                                    <div class="col-lg-2 col-md-4 col-6">
                                        <div class="small-box bg-secondary">
                                            <div class="inner">
                                                <h3>${stats.todayTodoCount}</h3>
                                                <p>今日待跟进</p>
                                            </div>
                                            <div class="icon"><i class="fas fa-calendar-check"></i></div><span
                                                class="small-box-footer">及时完成计划</span>
                                        </div>
                                    </div>
                                    <div class="col-lg-2 col-md-4 col-6">
                                        <div class="small-box bg-danger">
                                            <div class="inner">
                                                <h3>${stats.warningCount}</h3>
                                                <p>高风险客户</p>
                                            </div>
                                            <div class="icon"><i class="fas fa-exclamation-triangle"></i></div><a
                                                href="${pageContext.request.contextPath}/warning/list"
                                                class="small-box-footer">立即处理 <i
                                                    class="fas fa-arrow-circle-right"></i></a>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-lg-7">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title"><i
                                                        class="fas fa-filter text-primary mr-2"></i>销售漏斗概览</h3><a
                                                    href="${pageContext.request.contextPath}/funnel"
                                                    class="float-right">查看详情</a>
                                            </div>
                                            <div class="card-body">
                                                <div id="funnel" class="funnel-container"></div>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="col-lg-5">
                                        <div class="card">
                                            <div class="card-header">
                                                <h3 class="card-title"><i
                                                        class="fas fa-bell text-warning mr-2"></i>今日待跟进</h3>
                                            </div>
                                            <div class="card-body p-0">
                                                <div class="table-responsive">
                                                    <table class="table mb-0">
                                                        <thead>
                                                            <tr>
                                                                <th>时间</th>
                                                                <th>客户</th>
                                                                <th>计划</th>
                                                                <th></th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach items="${todayFollows}" var="f">
                                                                <tr class="table-warning">
                                                                    <td>${f.nextFollowTime}</td>
                                                                    <td>
                                                                        <c:out value="${f.customerName}" />
                                                                    </td>
                                                                    <td>
                                                                        <c:out value="${f.nextPlan}" />
                                                                    </td>
                                                                    <td><a href="${pageContext.request.contextPath}/follow/add?customerId=${f.customerId}"
                                                                            class="btn btn-xs btn-primary">跟进</a></td>
                                                                </tr>
                                                            </c:forEach>
                                                            <c:if test="${empty todayFollows}">
                                                                <tr>
                                                                    <td colspan="4">
                                                                        <div class="empty-state"><i
                                                                                class="far fa-calendar-check"></i>
                                                                            <div>今天没有待跟进事项</div>
                                                                        </div>
                                                                    </td>
                                                                </tr>
                                                            </c:if>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="card">
                                    <div class="card-header">
                                        <h3 class="card-title"><i class="fas fa-history text-success mr-2"></i>最近跟进记录
                                        </h3>
                                    </div>
                                    <div class="card-body p-0">
                                        <div class="table-responsive">
                                            <table class="table mb-0">
                                                <thead>
                                                    <tr>
                                                        <th>跟进时间</th>
                                                        <th>客户</th>
                                                        <th>商机</th>
                                                        <th>方式</th>
                                                        <th>跟进内容</th>
                                                        <th>跟进人</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach items="${latestFollows}" var="f">
                                                        <tr>
                                                            <td>${f.followTime}</td>
                                                            <td>
                                                                <c:out value="${f.customerName}" />
                                                            </td>
                                                            <td>
                                                                <c:out value="${f.opportunityTitle}" />
                                                            </td>
                                                            <td><span class="badge badge-info">${f.followType}</span>
                                                            </td>
                                                            <td>
                                                                <c:out value="${f.followContent}" />
                                                            </td>
                                                            <td>${f.followUserName}</td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </section>
                    </div>
                    <jsp:include page="footer.jsp" />
                </div>
                <jsp:include page="scripts.jsp" />
                <script src="https://cdn.jsdelivr.net/npm/echarts@5.4/dist/echarts.min.js"></script>
                <script>const raw = ${ funnelJson }; const chart = echarts.init(document.getElementById('funnel')); chart.setOption({ tooltip: { trigger: 'item' }, series: [{ type: 'funnel', left: '8%', top: 20, bottom: 20, width: '84%', minSize: '25%', maxSize: '100%', sort: 'descending', gap: 3, label: { show: true, position: 'inside', formatter: '{b}  {c}' }, itemStyle: { borderColor: '#fff', borderWidth: 2, borderRadius: 7 }, data: raw.map(x => ({ name: x.name, value: x.value })) }] }); window.addEventListener('resize', () => chart.resize());</script>
            </body>

            </html>