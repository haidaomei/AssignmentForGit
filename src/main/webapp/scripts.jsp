<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.6/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/admin-lte@3.2/dist/js/adminlte.min.js"></script>
    <script>if (location.pathname.includes('/contract/list')) { document.querySelectorAll('table tbody tr').forEach(r => { if (r.cells.length < 8 || !r.cells[6].innerText.includes('执行中')) return; const end = new Date(r.cells[5].innerText.trim() + 'T00:00:00'), now = new Date(); now.setHours(0, 0, 0, 0); const days = (end - now) / 86400000; if (days >= 0 && days <= 30) r.classList.add('table-warning') }) }</script>