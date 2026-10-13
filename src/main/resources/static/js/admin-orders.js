/* ================================================
   TRADE JAPAN — Admin: Inquiries (Messages) & Purchases — with search / filters
   Uses apiFetch / showToast / showConfirm / escapeHtml from the dashboard + app.js
   Uses oMoney / oDate / tjErr / purchase helpers from orders.js
   ================================================ */

const INQUIRY_STATUSES = [['NEW', 'New'], ['CONTACTED', 'Contacted'], ['IN_PROGRESS', 'In Progress'], ['CLOSED', 'Closed']];
let adminInquiries = [];
let adminPurchases = [];
let currentInquiryId = null;

/* ======================= INQUIRIES ======================= */
const distinctSorted = arr => [...new Set(arr.filter(Boolean))].sort((a, b) => a.localeCompare(b));

/* fill a filter <select> and keep whatever is chosen right now */
function fillFilter(id, pairs, firstLabel) {
    const el = document.getElementById(id);
    if (!el) return;
    const current = el.value;
    el.innerHTML = `<option value="">${firstLabel}</option>` +
        pairs.map(([v, label]) => `<option value="${escapeHtml(String(v))}">${escapeHtml(label)}</option>`).join('');
    el.value = pairs.some(([v]) => String(v) === current) ? current : '';
}

/* the "Admin" filter is only for the Super Admin */
function isSuperAdmin() { return getRole() === 'ROLE_SUPER_ADMIN'; }
function showAdminFilters() {
    ['inq-f-admin-wrap', 'pur-f-admin-wrap'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = isSuperAdmin() ? '' : 'none';
    });
}

const fval = id => (document.getElementById(id)?.value || '').trim();

async function loadAdminInquiries() {
    const tbody = document.getElementById('inquiries-list');
    const res = await apiFetch('/admin/inquiries');
    if (!res.ok || !Array.isArray(res.data)) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">Could not load inquiries.</td></tr>';
        return;
    }
    adminInquiries = res.data;
    showAdminFilters();
    fillFilter('inq-f-make', distinctSorted(adminInquiries.map(i => i.vehicleMake)).map(v => [v, v]), 'All Makes');
    fillFilter('inq-f-dest', distinctSorted(adminInquiries.map(i => i.destinationCountry)).map(v => [v, v]), 'All Countries');
    const admins = new Map();
    adminInquiries.forEach(i => (i.handledBy || []).forEach(a => admins.set(String(a.id), a.name)));
    fillFilter('inq-f-admin', [...admins.entries()].sort((a, b) => a[1].localeCompare(b[1])), 'All Admins');
    renderInquiries();
}

function getFilteredInquiries() {
    const q = fval('inq-f-search').toLowerCase();
    const make = fval('inq-f-make'), dest = fval('inq-f-dest'), status = fval('inq-f-status');
    const from = fval('inq-f-from'), to = fval('inq-f-to');
    const adminId = isSuperAdmin() ? fval('inq-f-admin') : '';
    return adminInquiries.filter(i => {
        const day = (i.createdAt || '').slice(0, 10);
        return (!q || `${i.customerName || ''} ${i.customerEmail || ''}`.toLowerCase().includes(q))
            && (!make || i.vehicleMake === make)
            && (!dest || i.destinationCountry === dest)
            && (!status || i.status === status)
            && (!from || day >= from)
            && (!to || day <= to)
            && (!adminId || (i.handledBy || []).some(a => String(a.id) === adminId));
    });
}

function inquiryFiltersChanged() { renderInquiries(); }

function clearInquiryFilters() {
    ['inq-f-search', 'inq-f-make', 'inq-f-dest', 'inq-f-status', 'inq-f-from', 'inq-f-to', 'inq-f-admin']
        .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
    renderInquiries();
}

async function refreshInquiries() {
    await loadAdminInquiries();
    showToast('Inquiries refreshed.');
}

function renderInquiries() {
    const tbody = document.getElementById('inquiries-list');
    const footer = document.getElementById('inquiries-footer');
    const list = getFilteredInquiries();

    if (!list.length) {
        const msg = adminInquiries.length ? 'No inquiries match your filters.' : 'No inquiries yet.';
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">${msg}</td></tr>`;
        footer.innerHTML = '<span class="pager-text">Showing 0 inquiries</span>';
        return;
    }

    tbody.innerHTML = list.map(i => `
        <tr class="${i.unreadCount > 0 ? 'row-unread' : ''}">
            <td>
                <div class="user-cell">
                    <div class="user-avatar"><i class="bi bi-person-fill"></i></div>
                    <div class="user-info">
                        <div class="user-name">${escapeHtml(i.customerName || '')}</div>
                        <div class="user-role">${escapeHtml(i.customerEmail || '')}</div>
                    </div>
                </div>
            </td>
            <td>${escapeHtml(i.vehicleName || '')}</td>
            <td style="max-width:260px;">
                <div style="font-weight:600;">${escapeHtml(i.subject || '')}</div>
                <div style="font-size:.85rem;color:var(--tj-text-muted);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:260px;">${escapeHtml(i.message || '')}</div>
            </td>
            <td>${escapeHtml(i.destinationCountry || '')}</td>
            <td>
                <select class="status-select" data-prev="${i.status}" onchange="changeInquiryStatus(${i.inquiryId}, this)">
                    ${INQUIRY_STATUSES.map(s => `<option value="${s[0]}" ${i.status === s[0] ? 'selected' : ''}>${s[1]}</option>`).join('')}
                </select>
            </td>
            <td>${oDate(i.createdAt)}</td>
            <td>
                <div style="display:flex;gap:8px;align-items:center;">
                    <button class="action-btn" title="${i.unreadCount > 0 ? i.unreadCount + ' unread message' + (i.unreadCount === 1 ? '' : 's') : 'View / Reply'}" onclick="openInquiry(${i.inquiryId})">
                        <i class="bi bi-chat-dots"></i>${i.unreadCount > 0 ? `<span class="reply-dot">${i.unreadCount}</span>` : ''}
                    </button>
                    ${i.purchaseId
                        ? `<span class="p-status p-status-fully-paid" title="Purchase #${i.purchaseId}">Purchase #${i.purchaseId}</span>`
                        : `<button class="action-btn" title="Create purchase" onclick="openCreatePurchase(${i.inquiryId})"><i class="bi bi-bag-plus"></i></button>`}
                    <button class="action-btn delete" title="Delete" onclick="deleteInquiry(${i.inquiryId})"><i class="bi bi-trash"></i></button>
                </div>
            </td>
        </tr>`).join('');

    footer.innerHTML = `<span class="pager-text">Showing ${list.length === adminInquiries.length ? list.length : list.length + ' of ' + adminInquiries.length} inquiries</span>`;
}

function changeInquiryStatus(id, select) {
    const prev = select.dataset.prev;
    const next = select.value;
    if (prev === next) return;
    const label = k => (INQUIRY_STATUSES.find(x => x[0] === k) || [k, k])[1];
    showConfirm(
        `Change this inquiry from “${label(prev)}” to “${label(next)}”?`,
        async () => {
            const res = await apiFetch(`/admin/inquiries/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: next }) });
            if (res.ok) {
                select.dataset.prev = next;
                const it = adminInquiries.find(x => x.inquiryId === id);
                if (it) it.status = next;
                showToast('Status updated.');
            } else { select.value = prev; showToast(tjErr(res, 'Failed to update status.'), true); }
        },
        () => { select.value = prev; }
    );
}

function deleteInquiry(id) {
    showConfirm('Delete this inquiry and its replies?', async () => {
        const res = await apiFetch(`/admin/inquiries/${id}`, { method: 'DELETE' });
        if (res.ok) { showToast('Inquiry deleted.'); loadAdminInquiries(); }
        else showToast(tjErr(res, 'Failed to delete inquiry.'), true);
    });
}

async function openInquiry(id) {
    const i = adminInquiries.find(x => x.inquiryId === id);
    if (!i) return;
    currentInquiryId = id;
    document.getElementById('inqModalTitle').textContent = i.subject;
    document.getElementById('inqModalInfo').innerHTML = `
        <div class="detail-grid">
            <div><span>Customer</span><strong>${escapeHtml(i.customerName || '')}</strong><small>${escapeHtml(i.customerEmail || '')}</small></div>
            <div><span>Vehicle</span><strong>${escapeHtml(i.vehicleName || '')}</strong><small><a href="/pages/vehicle-detail.html?id=${i.vehicleId}" target="_blank">View vehicle</a></small></div>
            <div><span>Destination</span><strong>${escapeHtml(i.destinationCountry || '')}</strong></div>
            <div><span>Received</span><strong>${oDateTime(i.createdAt)}</strong></div>
        </div>`;
    document.getElementById('inqReplyText').value = '';
    document.getElementById('inqReplyError').style.display = 'none';
    document.getElementById('inqThread').innerHTML = '<div class="thread-note">Loading conversation...</div>';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('inquiryModal')).show();
    await refreshInquiryThread(i);
    markInquiryRead(id);   // opening the chat clears the unread counter
}

/* tell the server the admins have now read everything the customer wrote */
async function markInquiryRead(id) {
    const i = adminInquiries.find(x => x.inquiryId === id);
    if (!i || !(i.unreadCount > 0)) return;
    const res = await apiFetch(`/admin/inquiries/${id}/read`, { method: 'POST' });
    if (res.ok) { i.unreadCount = 0; renderInquiries(); }
}

async function refreshInquiryThread(i) {
    const res = await apiFetch(`/inquiries/${i.inquiryId}/replies`);
    const replies = (res.ok && Array.isArray(res.data)) ? res.data : [];
    document.getElementById('inqThread').innerHTML = `
        <div class="thread-bubble mine">
            <div class="thread-author">${escapeHtml(i.customerName || 'Customer')} &middot; ${oDateTime(i.createdAt)}</div>
            <div class="thread-text">${escapeHtml(i.message || '')}</div>
        </div>
        ${replies.map(r => r.fromAdmin ? `
            <div class="thread-bubble admin">
                <div class="thread-author">${escapeHtml(r.staffName || r.authorName)} (Trade Japan) &middot; ${oDateTime(r.createdAt)}</div>
                <div class="thread-text">${escapeHtml(r.message)}</div>
            </div>` : `
            <div class="thread-bubble mine">
                <div class="thread-author">${escapeHtml(r.authorName || i.customerName || 'Customer')} &middot; ${oDateTime(r.createdAt)}</div>
                <div class="thread-text">${escapeHtml(r.message)}</div>
            </div>`).join('')}`;
    const t = document.getElementById('inqThread');
    t.scrollTop = t.scrollHeight;
}

/* the admin can send as many replies as needed */
async function sendInquiryReply() {
    const text = document.getElementById('inqReplyText').value.trim();
    const err = document.getElementById('inqReplyError');
    if (!text) { err.textContent = 'Please type a reply.'; err.style.display = 'block'; return; }
    const res = await apiFetch(`/admin/inquiries/${currentInquiryId}/replies`, { method: 'POST', body: JSON.stringify({ message: text }) });
    if (res.ok) {
        showToast('Reply sent.');
        document.getElementById('inqReplyText').value = '';
        err.style.display = 'none';
        await loadAdminInquiries();
        const i = adminInquiries.find(x => x.inquiryId === currentInquiryId);
        if (i) await refreshInquiryThread(i);
    } else if (res.status !== 401 && res.status !== 403) {
        err.textContent = tjErr(res, 'Failed to send reply.');
        err.style.display = 'block';
    }
}

/* ======================= PURCHASES ======================= */
async function loadAdminPurchases() {
    const tbody = document.getElementById('purchases-list');
    const res = await apiFetch('/admin/purchases');
    if (!res.ok || !Array.isArray(res.data)) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:32px;color:var(--tj-text-muted);">Could not load purchases.</td></tr>';
        return;
    }
    adminPurchases = res.data;
    showAdminFilters();
    fillFilter('pur-f-make', distinctSorted(adminPurchases.map(p => p.vehicleMake)).map(v => [v, v]), 'All Makes');
    fillFilter('pur-f-dest', distinctSorted(adminPurchases.map(p => p.destinationCountry)).map(v => [v, v]), 'All Countries');
    const admins = new Map();
    adminPurchases.forEach(p => { if (p.processedBy != null) admins.set(String(p.processedBy), p.processedByName || ('Admin #' + p.processedBy)); });
    fillFilter('pur-f-admin', [...admins.entries()].sort((a, b) => a[1].localeCompare(b[1])), 'All Admins');
    renderPurchases();
}

function getFilteredPurchases() {
    const q = fval('pur-f-search').toLowerCase();
    const make = fval('pur-f-make'), dest = fval('pur-f-dest'), status = fval('pur-f-status');
    const from = fval('pur-f-from'), to = fval('pur-f-to');
    const adminId = isSuperAdmin() ? fval('pur-f-admin') : '';
    return adminPurchases.filter(p => {
        const day = (p.createdAt || '').slice(0, 10);
        return (!q || `${p.customerName || ''} ${p.customerEmail || ''}`.toLowerCase().includes(q))
            && (!make || p.vehicleMake === make)
            && (!dest || p.destinationCountry === dest)
            && (!status || p.status === status)
            && (!from || day >= from)
            && (!to || day <= to)
            && (!adminId || String(p.processedBy) === adminId);
    });
}

function purchaseFiltersChanged() { renderPurchases(); }

function clearPurchaseFilters() {
    ['pur-f-search', 'pur-f-make', 'pur-f-dest', 'pur-f-status', 'pur-f-from', 'pur-f-to', 'pur-f-admin']
        .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
    renderPurchases();
}

async function refreshPurchases() {
    await loadAdminPurchases();
    showToast('Purchases refreshed.');
}

function renderPurchases() {
    const tbody = document.getElementById('purchases-list');
    const footer = document.getElementById('purchases-footer');
    const list = getFilteredPurchases();

    if (!list.length) {
        const msg = adminPurchases.length ? 'No purchases match your filters.' : 'No purchases yet.';
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:32px;color:var(--tj-text-muted);">${msg}</td></tr>`;
        footer.innerHTML = '<span class="pager-text">Showing 0 purchases</span>';
        return;
    }
    tbody.innerHTML = list.map(p => `
        <tr>
            <td><strong>#${p.purchaseId}</strong></td>
            <td>
                <div class="user-info">
                    <div class="user-name">${escapeHtml(p.customerName || '')}</div>
                    <div class="user-role">${escapeHtml(p.customerEmail || '')}</div>
                </div>
            </td>
            <td>
                ${escapeHtml(p.vehicleName || '')}
                ${p.destinationCountry ? `<div class="vt-sub"><i class="bi bi-geo-alt"></i> ${escapeHtml(p.destinationCountry)}</div>` : ''}
            </td>
            <td><strong>${oMoney(p.totalPrice)}</strong></td>
            <td>${oMoney(p.advancePaid)}</td>
            <td>${purchaseStatusPill(p.status)}</td>
            <td>${p.invoiceNo ? escapeHtml(p.invoiceNo) : '—'}</td>
            <td>
                <button class="action-btn" title="View / Manage" onclick="openPurchase(${p.purchaseId})"><i class="bi bi-eye"></i></button>
            </td>
        </tr>`).join('');
    footer.innerHTML = `<span class="pager-text">Showing ${list.length === adminPurchases.length ? list.length : list.length + ' of ' + adminPurchases.length} purchases</span>`;
}

/* ---------- create ---------- */
function openCreatePurchase(inquiryId) {
    const i = inquiryId ? adminInquiries.find(x => x.inquiryId === inquiryId) : null;
    document.getElementById('pc-inquiry').value = i ? i.inquiryId : '';
    document.getElementById('pc-linked').style.display = i ? 'block' : 'none';
    document.getElementById('pc-direct').style.display = i ? 'none' : 'flex';
    document.getElementById('pc-linked').innerHTML = i
        ? `<i class="bi bi-link-45deg"></i> Linked to inquiry <strong>#${i.inquiryId}</strong> &mdash; ${escapeHtml(i.customerName || '')} &middot; ${escapeHtml(i.vehicleName || '')}`
        : '';
    document.getElementById('pc-email').value = '';
    document.getElementById('pc-vehicle').value = '';
    document.getElementById('pc-destination').innerHTML = countryOptionsHtml();
    document.getElementById('pc-total').value = i && i.vehiclePrice ? i.vehiclePrice : '';
    document.getElementById('pc-advance').value = '';
    document.getElementById('pc-notes').value = '';
    document.getElementById('purchaseCreateError').style.display = 'none';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('purchaseCreateModal')).show();
}

async function saveNewPurchase() {
    const err = document.getElementById('purchaseCreateError');
    const inquiryId = document.getElementById('pc-inquiry').value;
    const total = document.getElementById('pc-total').value;
    const advance = document.getElementById('pc-advance').value;
    const email = document.getElementById('pc-email').value.trim();
    const vehicle = document.getElementById('pc-vehicle').value.trim();
    if (!total || Number(total) <= 0) { err.textContent = 'Enter the total price.'; err.style.display = 'block'; return; }
    if (!inquiryId && (!email || !vehicle)) { err.textContent = 'Buyer email and vehicle ID are required when no inquiry is linked.'; err.style.display = 'block'; return; }

    const payload = {
        inquiryId: inquiryId ? Number(inquiryId) : null,
        buyerEmail: inquiryId ? null : email,
        vehicleId: inquiryId ? null : Number(vehicle),
        destinationCountry: inquiryId ? null : (document.getElementById('pc-destination').value || null),
        totalPrice: Number(total),
        advancePaid: advance === '' ? 0 : Number(advance),
        notes: document.getElementById('pc-notes').value.trim() || null
    };
    const res = await apiFetch('/admin/purchases', { method: 'POST', body: JSON.stringify(payload) });
    if (res.ok) {
        bootstrap.Modal.getInstance(document.getElementById('purchaseCreateModal')).hide();
        showToast(`Purchase #${res.data.purchaseId} created.`);
        loadAdminInquiries();
        showAdminSection('purchases');
    } else if (res.status !== 401 && res.status !== 403) {
        err.textContent = tjErr(res, 'Failed to create purchase.');
        err.style.display = 'block';
    }
}

/* ---------- detail / manage ---------- */
function nextPurchaseStatus(current) {
    const i = PURCHASE_STEPS.findIndex(s => s[0] === current);
    return i >= 0 && i < PURCHASE_STEPS.length - 1 ? PURCHASE_STEPS[i + 1] : null;
}

async function openPurchase(id, showModal = true) {
    const res = await apiFetch(`/purchases/${id}`);
    if (!res.ok || !res.data || typeof res.data !== 'object') { showToast(tjErr(res, 'Purchase not found.'), true); return; }
    const p = res.data;
    const next = nextPurchaseStatus(p.status);

    document.getElementById('purModalTitle').textContent = `Purchase #${p.purchaseId}`;
    document.getElementById('purModalBody').innerHTML = `
        ${purchaseStepsHtml(p.status)}
        <div class="detail-grid" style="margin-top:1.2rem;">
            <div><span>Customer</span><strong>${escapeHtml(p.customerName || '')}</strong><small>${escapeHtml(p.customerEmail || '')}</small></div>
            <div><span>Vehicle</span><strong>${escapeHtml(p.vehicleName || '')}</strong></div>
            <div><span>Total Price</span><strong>${oMoney(p.totalPrice)}</strong></div>
            <div><span>Balance Due</span><strong class="${Number(p.balance) > 0 ? 'text-due' : ''}">${oMoney(p.balance)}</strong></div>
            <div><span>Processed By</span><strong>${escapeHtml(p.processedByName || '')}</strong></div>
            <div><span>Linked Inquiry</span><strong>${p.inquiryId ? '#' + p.inquiryId : 'None'}</strong></div>
            <div><span>Created</span><strong>${oDate(p.createdAt)}</strong></div>
            <div><span>Invoice</span><strong>${p.invoiceNo ? escapeHtml(p.invoiceNo) : 'Not uploaded'}</strong>
                ${p.invoiceFileUrl ? `<small><a href="#" onclick="downloadInvoice(${p.purchaseId});return false;">Download PDF</a></small>` : ''}</div>
        </div>
        ${p.notes ? `<div class="purchase-notes" style="margin-top:1rem;"><i class="bi bi-info-circle"></i> ${escapeHtml(p.notes)}</div>` : ''}

        <hr style="margin:1.4rem 0;">
        <h6 class="text-tj-navy">Advance Payment</h6>
        <div class="inline-form">
            <input type="number" class="form-control" id="pur-advance" min="0" step="0.01" value="${Number(p.advancePaid || 0).toFixed(2)}">
            <button class="btn-tj-outline" onclick="updatePurchaseAdvance(${p.purchaseId})">Update</button>
        </div>

        <h6 class="text-tj-navy" style="margin-top:1.2rem;">Invoice (PDF, max 5 MB)</h6>
        <div class="inline-form">
            <input type="text" class="form-control" id="pur-invoice-no" placeholder="Invoice number" value="${escapeHtml(p.invoiceNo || '')}">
            <input type="file" class="form-control" id="pur-invoice-file" accept="application/pdf">
            <button class="btn-tj-outline" onclick="uploadPurchaseInvoice(${p.purchaseId})"><i class="bi bi-upload"></i> Upload</button>
        </div>

        <div id="purModalError" class="text-danger small mt-3" style="display:none;"></div>
        <div style="margin-top:1.6rem;display:flex;justify-content:flex-end;">
            ${next
                ? `<button class="btn-tj-primary" onclick="movePurchaseNext(${p.purchaseId}, '${p.status}', '${next[0]}', '${next[1]}')"><i class="bi bi-arrow-right-circle"></i> Move to ${next[1]}</button>`
                : '<span class="p-status p-status-delivered">Completed — Delivered</span>'}
        </div>`;
    if (showModal) bootstrap.Modal.getOrCreateInstance(document.getElementById('purchaseModal')).show();
}

function purchaseModalError(res, fallback) {
    const el = document.getElementById('purModalError');
    if (!el) { showToast(tjErr(res, fallback), true); return; }
    el.textContent = tjErr(res, fallback);
    el.style.display = 'block';
}

async function updatePurchaseAdvance(id) {
    const value = document.getElementById('pur-advance').value;
    if (value === '' || Number(value) < 0) { purchaseModalError(null, 'Enter a valid amount.'); return; }
    const res = await apiFetch(`/admin/purchases/${id}/advance`, { method: 'PATCH', body: JSON.stringify({ advancePaid: Number(value) }) });
    if (res.ok) { showToast('Advance payment updated.'); await openPurchase(id, false); loadAdminPurchases(); }
    else if (res.status !== 401 && res.status !== 403) purchaseModalError(res, 'Failed to update advance payment.');
}

async function uploadPurchaseInvoice(id) {
    const invoiceNo = document.getElementById('pur-invoice-no').value.trim();
    const file = document.getElementById('pur-invoice-file').files[0];
    if (!invoiceNo) { purchaseModalError(null, 'Enter an invoice number.'); return; }
    if (!file) { purchaseModalError(null, 'Choose a PDF file.'); return; }
    const form = new FormData();
    form.append('invoiceNo', invoiceNo);
    form.append('file', file);
    const res = await apiFetch(`/admin/purchases/${id}/invoice`, { method: 'POST', body: form });
    if (res.ok) { showToast('Invoice uploaded.'); await openPurchase(id, false); loadAdminPurchases(); }
    else if (res.status !== 401 && res.status !== 403) purchaseModalError(res, 'Failed to upload invoice.');
}

function movePurchaseNext(id, current, nextKey, nextLabel) {
    showConfirm(`Move purchase #${id} to “${nextLabel}”?`, async () => {
        const res = await apiFetch(`/admin/purchases/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: nextKey }) });
        if (res.ok) { showToast('Status updated.'); await openPurchase(id, false); loadAdminPurchases(); }
        else if (res.status !== 401 && res.status !== 403) purchaseModalError(res, 'Failed to update status.');
    });
}
