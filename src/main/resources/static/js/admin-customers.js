/* ================================================
   TRADE JAPAN — Admin: Customers (view + activate / deactivate; no editing of customer data)
   Uses apiFetch / showToast / showConfirm / escapeHtml from the dashboard + app.js,
   oDate / oDateTime / oMoney / tjErr / purchaseStatusPill from orders.js,
   fillFilter / fval / INQUIRY_STATUSES from admin-orders.js
   ================================================ */

const CUSTOMER_STATUSES = [['ACTIVE', 'Active'], ['INACTIVE', 'Inactive']];
let adminCustomers = [];

const customerName = c => ((c.firstName || '') + ' ' + (c.lastName || '')).trim() || c.email;

async function loadAdminCustomers() {
    const res = await apiFetch('/admin/customers');
    if (!res.ok || !Array.isArray(res.data)) {
        document.getElementById('customers-list').innerHTML =
            '<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">Could not load customers.</td></tr>';
        return;
    }
    adminCustomers = res.data;
    renderCustomers();
}

function getFilteredCustomers() {
    const q = fval('cus-f-search').toLowerCase();
    const status = fval('cus-f-status'), from = fval('cus-f-from'), to = fval('cus-f-to');
    return adminCustomers.filter(c => {
        const day = (c.createdAt || '').slice(0, 10);
        return (!q || `${customerName(c)} ${c.email} ${c.whatsappNo || ''}`.toLowerCase().includes(q))
            && (!status || c.status === status)
            && (!from || day >= from)
            && (!to || day <= to);
    });
}

function customerFiltersChanged() { renderCustomers(); }

function clearCustomerFilters() {
    ['cus-f-search', 'cus-f-status', 'cus-f-from', 'cus-f-to']
        .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
    renderCustomers();
}

async function refreshCustomers() {
    await loadAdminCustomers();
    showToast('Customers refreshed.');
}

function renderCustomers() {
    const tbody = document.getElementById('customers-list');
    const footer = document.getElementById('customers-footer');
    const list = getFilteredCustomers();
    if (!list.length) {
        const msg = adminCustomers.length ? 'No customers match your filters.' : 'No customers have registered yet.';
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">${msg}</td></tr>`;
        footer.innerHTML = '<span class="pager-text">Showing 0 customers</span>';
        return;
    }
    tbody.innerHTML = list.map(c => `
        <tr class="${c.status === 'INACTIVE' ? 'row-inactive' : ''}">
            <td>
                <div class="user-cell">
                    <div class="user-avatar"><i class="bi bi-person-fill"></i></div>
                    <div class="user-info">
                        <div class="user-name">${escapeHtml(customerName(c))}</div>
                        <div class="user-role">${escapeHtml(c.email)}</div>
                    </div>
                </div>
            </td>
            <td>${c.whatsappNo ? escapeHtml(c.whatsappNo) : '—'}</td>
            <td>${oDate(c.createdAt)}</td>
            <td>${c.inquiryCount}</td>
            <td>${c.purchaseCount}</td>
            <td>
                <select class="status-select" data-prev="${c.status}" onchange="changeCustomerStatus(${c.id}, this)">
                    ${CUSTOMER_STATUSES.map(s => `<option value="${s[0]}" ${c.status === s[0] ? 'selected' : ''}>${s[1]}</option>`).join('')}
                </select>
            </td>
            <td>
                <button class="action-btn" title="View details" onclick="openCustomer(${c.id})"><i class="bi bi-eye"></i></button>
            </td>
        </tr>`).join('');
    footer.innerHTML = `<span class="pager-text">Showing ${list.length === adminCustomers.length ? list.length : list.length + ' of ' + adminCustomers.length} customers</span>`;
}

function changeCustomerStatus(id, select) {
    const prev = select.dataset.prev, next = select.value;
    if (prev === next) return;
    const c = adminCustomers.find(x => x.id === id);
    const name = c ? escapeHtml(customerName(c)) : 'this customer';
    const text = next === 'INACTIVE'
        ? `Deactivate <b>${name}</b>? They will no longer be able to log in.`
        : `Reactivate <b>${name}</b>? They will be able to log in again.`;
    showConfirm(
        text,
        async () => {
            const res = await apiFetch(`/admin/customers/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: next }) });
            if (res.ok) {
                select.dataset.prev = next;
                if (c) c.status = next;
                showToast(next === 'INACTIVE' ? 'Customer deactivated.' : 'Customer reactivated.');
                renderCustomers();
            } else {
                select.value = prev;
                if (res.status !== 401 && res.status !== 403) showToast(tjErr(res, 'Failed to update the customer.'), true);
            }
        },
        () => { select.value = prev; }
    );
}

/* ---------- read-only detail window ---------- */
function showCustomerTab(name) {
    document.getElementById('cus-tab-inq').style.display = name === 'inq' ? 'block' : 'none';
    document.getElementById('cus-tab-pur').style.display = name === 'pur' ? 'block' : 'none';
    document.getElementById('custab-inq').classList.toggle('active', name === 'inq');
    document.getElementById('custab-pur').classList.toggle('active', name === 'pur');
}

async function openCustomer(id) {
    const res = await apiFetch(`/admin/customers/${id}`);
    if (!res.ok || !res.data || !res.data.profile) {
        if (res.status !== 401 && res.status !== 403) showToast(tjErr(res, 'Could not load the customer.'), true);
        return;
    }
    const { profile: p, inquiries, purchases } = res.data;
    const inqStatus = k => (INQUIRY_STATUSES.find(s => s[0] === k) || [k, k])[1];

    document.getElementById('cusModalTitle').textContent = customerName(p);
    document.getElementById('cusModalInfo').innerHTML = `
        <div class="detail-grid">
            <div><span>Email</span><strong>${escapeHtml(p.email)}</strong></div>
            <div><span>WhatsApp</span><strong>${p.whatsappNo ? escapeHtml(p.whatsappNo) : '—'}</strong></div>
            <div><span>Joined</span><strong>${oDate(p.createdAt)}</strong></div>
            <div><span>Account</span><strong>${p.status === 'ACTIVE' ? 'Active' : 'Inactive'} &middot; ${p.authProvider === 'GOOGLE' ? 'Google sign-in' : 'Email &amp; password'}</strong></div>
        </div>`;
    document.getElementById('custab-inq').textContent = `Inquiries (${inquiries.length})`;
    document.getElementById('custab-pur').textContent = `Purchases (${purchases.length})`;

    document.getElementById('cus-tab-inq').innerHTML = inquiries.length
        ? `<table class="admin-table"><thead><tr><th>Vehicle</th><th>Subject</th><th>Destination</th><th>Status</th><th>Date</th></tr></thead><tbody>
            ${inquiries.map(i => `<tr>
                <td>${escapeHtml(i.vehicleName || '')}</td>
                <td>${escapeHtml(i.subject || '')}</td>
                <td>${escapeHtml(i.destinationCountry || '')}</td>
                <td>${escapeHtml(inqStatus(i.status))}</td>
                <td>${oDate(i.createdAt)}</td></tr>`).join('')}
           </tbody></table>`
        : '<p class="text-muted" style="padding:1rem 0;">This customer has not sent any inquiries.</p>';

    document.getElementById('cus-tab-pur').innerHTML = purchases.length
        ? `<table class="admin-table"><thead><tr><th>#</th><th>Vehicle</th><th>Total</th><th>Status</th><th>Date</th></tr></thead><tbody>
            ${purchases.map(x => `<tr>
                <td><strong>#${x.purchaseId}</strong></td>
                <td>${escapeHtml(x.vehicleName || '')}</td>
                <td>${oMoney(x.totalPrice)}</td>
                <td>${purchaseStatusPill(x.status)}</td>
                <td>${oDate(x.createdAt)}</td></tr>`).join('')}
           </tbody></table>`
        : '<p class="text-muted" style="padding:1rem 0;">This customer has no purchases yet.</p>';

    showCustomerTab('inq');
    bootstrap.Modal.getOrCreateInstance(document.getElementById('customerModal')).show();
}
