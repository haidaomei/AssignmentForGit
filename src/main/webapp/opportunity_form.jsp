<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><!DOCTYPE html>
<html lang="zh-CN">
<head><jsp:include page="header.jsp" /></head>
<body class="hold-transition sidebar-mini">
	<div class="wrapper"><jsp:include page="navbar.jsp" /><jsp:include page="sidebar.jsp" /><div class="content-wrapper">
			<div class="content-header">
				<div class="container-fluid">
					<h1>${empty opportunity?'新增商机':'编辑商机'}</h1>
				</div>
			</div>
			<section class="content">
				<div class="container-fluid">
					<form method="post" action="${pageContext.request.contextPath}/opportunity/save" id="oppForm">
						<input type="hidden" name="id" value="${opportunity.id}">
						<div class="card">
							<div class="card-header">
								<h3 class="card-title">商机基本信息</h3>
							</div>
							<div class="card-body">
								<div class="row">
									<div class="form-group col-md-6">
										<label>商机标题 *</label>
										<input name="title" value="${opportunity.title}" class="form-control" required>
									</div>
									<div class="form-group col-md-3">
										<label>客户 *</label>
										<select name="customerId" id="customerId" class="form-control" required>
											<option value="">请选择</option>
											<c:forEach items="${customerList}" var="c">
												<option value="${c.id}" ${opportunity.customerId==c.id||(empty opportunity&&selectedCustomerId==c.id)?'selected':''}>${c.customerName}</option>
											</c:forEach>
										</select>
									</div>
									<div class="form-group col-md-3">
										<label>主要联系人</label>
										<select name="contactId" id="contactId" class="form-control" data-selected="${opportunity.contactId}">
											<option value="">请先选择客户</option>
										</select>
									</div>
									<div class="form-group col-md-3">
										<label>当前阶段 *</label>
										<select name="stageId" class="form-control" required>
											<c:forEach items="${stageList}" var="s">
												<option value="${s.id}" ${opportunity.stageId==s.id||(empty opportunity&&s.sortOrder==1)?'selected':''}>${s.name}（${s.probability}%）</option>
											</c:forEach>
										</select>
									</div>
									<div class="form-group col-md-3">
										<label>预计成交日期</label>
										<input type="date" name="estimatedCloseDate" value="${opportunity.estimatedCloseDate}" class="form-control">
									</div>
									<div class="form-group col-md-3">
										<label>负责人</label>
										<select name="ownerUserId" class="form-control">
											<c:forEach items="${userList}" var="u">
												<option value="${u.id}" ${(empty opportunity&&u.id==sessionScope.user.id)||opportunity.ownerUserId==u.id?'selected':''}>${u.realName}</option>
											</c:forEach>
										</select>
									</div>
									<div class="form-group col-12">
										<label>商机描述</label>
										<textarea name="description" class="form-control" rows="3"><c:out value="${opportunity.description}" /></textarea>
									</div>
								</div>
							</div>
						</div>
						<div class="card">
							<div class="card-header">
								<h3 class="card-title">产品明细</h3>
								<button type="button" class="btn btn-sm btn-outline-primary float-right" id="addRow">
									<i class="fas fa-plus"></i>
									添加行
								</button>
							</div>
							<div class="card-body p-0">
								<div class="table-responsive">
									<table class="table line-items mb-0">
										<thead>
											<tr>
												<th style="width: 38%">产品</th>
												<th style="width: 14%">数量</th>
												<th style="width: 20%">单价</th>
												<th style="width: 20%">小计</th>
												<th></th>
											</tr>
										</thead>
										<tbody id="items">
											<c:forEach items="${opportunity.items}" var="item">
												<tr>
													<td>
														<select name="items[][productId]" class="form-control product" required>
															<option value="">请选择</option>
															<c:forEach items="${productList}" var="p">
																<option value="${p.id}" data-price="${p.unitPrice}" ${item.productId==p.id?'selected':''}>${p.productName}</option>
															</c:forEach>
														</select>
													</td>
													<td>
														<input name="items[][quantity]" class="form-control qty" type="number" min="1" value="${item.quantity}" required>
													</td>
													<td>
														<input name="items[][unitPrice]" class="form-control price" type="number" min="0" step="0.01" value="${item.unitPrice}" required>
													</td>
													<td class="money subtotal">¥${item.subtotal}</td>
													<td>
														<button type="button" class="btn btn-sm btn-danger remove">
															<i class="fas fa-times"></i>
														</button>
													</td>
												</tr>
											</c:forEach>
										</tbody>
										<tfoot>
											<tr>
												<td colspan="3" class="text-right">
													<strong>合计</strong>
												</td>
												<td colspan="2" class="money text-primary" id="total">¥0.00</td>
											</tr>
										</tfoot>
									</table>
								</div>
							</div>
							<div class="card-footer text-right">
								<a href="${pageContext.request.contextPath}/opportunity/list" class="btn btn-light">取消</a>
								<button class="btn btn-primary">
									<i class="fas fa-save"></i>
									保存商机
								</button>
							</div>
						</div>
					</form>
				</div>
			</section>
		</div><jsp:include page="footer.jsp" /></div><jsp:include page="scripts.jsp" /><template id="rowTemplate">
		<tr>
			<td>
				<select name="items[][productId]" class="form-control product" required>
					<option value="">请选择</option>
					<c:forEach items="${productList}" var="p">
						<option value="${p.id}" data-price="${p.unitPrice}">${p.productName}</option>
					</c:forEach>
				</select>
			</td>
			<td>
				<input name="items[][quantity]" class="form-control qty" type="number" min="1" value="1" required>
			</td>
			<td>
				<input name="items[][unitPrice]" class="form-control price" type="number" min="0" step="0.01" value="0" required>
			</td>
			<td class="money subtotal">¥0.00</td>
			<td>
				<button type="button" class="btn btn-sm btn-danger remove">
					<i class="fas fa-times"></i>
				</button>
			</td>
		</tr>
	</template>
	<script>
const ctx='${pageContext.request.contextPath}',items=document.getElementById('items');function addRow(){items.append(document.getElementById('rowTemplate').content.cloneNode(true))}function calc(){let total=0;items.querySelectorAll('tr').forEach(r=>{const q=parseFloat(r.querySelector('.qty').value)||0,p=parseFloat(r.querySelector('.price').value)||0,s=q*p;r.querySelector('.subtotal').textContent='¥'+s.toFixed(2);total+=s});document.getElementById('total').textContent='¥'+total.toFixed(2)}items.addEventListener('change',e=>{if(e.target.classList.contains('product')){const o=e.target.selectedOptions[0];e.target.closest('tr').querySelector('.price').value=o.dataset.price||0}calc()});items.addEventListener('input',calc);items.addEventListener('click',e=>{const b=e.target.closest('.remove');if(b){b.closest('tr').remove();if(!items.children.length)addRow();calc()}});document.getElementById('addRow').onclick=()=>{addRow();calc()};async function contacts(){const c=document.getElementById('customerId').value,s=document.getElementById('contactId'),selected=s.dataset.selected;s.innerHTML='<option value="">请选择</option>';if(!c)return;const data=await fetch(ctx+'/contact/options?customerId='+c).then(r=>r.json());data.forEach(x=>{const o=new Option(x.name,x.id);if(String(x.id)===selected)o.selected=true;s.add(o)})}document.getElementById('customerId').onchange=()=>{document.getElementById('contactId').dataset.selected='';contacts()};if(!items.children.length)addRow();contacts();calc();
</script>
</body>
</html>
