admin-orders.js/* ================================================
   TRADE JAPAN — Admin: Dashboard cards, Testimonials, Contact Messages, Reports
   Uses apiFetch / showToast / showConfirm / escapeHtml from the dashboard + app.js,
   oMoney / oDate / oDateTime / tjErr from orders.js,
   fillFilter / distinctSorted / fval / isSuperAdmin from admin-orders.js
   ================================================ */

const setText = (id, value) => { const el = document.getElementById(id); if (el) el.textContent = value; };

/* ======================= DASHBOARD ======================= */
async function loadDashboardStats() {
    const res = await apiFetch('/admin/dashboard-stats');
    if (!res.ok || !res.data) return;
    const s = res.data;
    setText('st-vehicles', s.totalVehicles);
    setText('st-customers', s.totalCustomers);
    setText('st-open', s.openInquiries);
    setText('st-purchases', s.purchasesLast30Days);
    setText('st-contact', s.newContactMessages);
    setText('st-pending', s.pendingTestimonials);
    setText('st-approved', s.approvedTestimonials);
    setText('st-rejected', s.rejectedTestimonials);

    const days = s.inquiriesLast7Days || [];
    const max = Math.max(1, ...days.map(d => d.count));
    document.getElementById('st-chart').innerHTML = days.map(d => {
        const label = new Date(d.date + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', day: 'numeric' });
        return `<div class="bar-col">
                    <div class="bar-val">${d.count}</div>
                    <div class="bar" style="height:${((d.count / max) * 8.5).toFixed(2)}rem"></div>
                    <div class="bar-lbl">${label}</div>
                </div>`;
    }).join('');
}

/* ======================= TESTIMONIALS ======================= */
let adminTestimonials = [];
let tmFilter = 'ALL';
const TM_STATUS = { PENDING: ['Pending', 'pending'], APPROVED: ['Approved', 'approved'], REJECTED: ['Rejected', 'rejected'] };

async function loadAdminTestimonials() {
    const res = await apiFetch('/admin/testimonials');
    if (!res.ok || !Array.isArray(res.data)) {
        document.getElementById('testimonials-list').innerHTML =
            '<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">Could not load testimonials.</td></tr>';
        return;
    }
    adminTestimonials = res.data;
    renderAdminTestimonials();
}

function setTestimonialFilter(f) {
    tmFilter = f;
    renderAdminTestimonials();
}

function renderAdminTestimonials() {
    const count = s => s === 'ALL' ? adminTestimonials.length : adminTestimonials.filter(t => t.status === s).length;
    [['ALL', 'All'], ['PENDING', 'Pending'], ['APPROVED', 'Approved'], ['REJECTED', 'Rejected']].forEach(([key, label]) => {
        const tab = document.getElementById('tm-tab-' + key);
        if (!tab) return;
        tab.textContent = `${label} (${count(key)})`;
        tab.classList.toggle('active', key === tmFilter);
    });

    const tbody = document.getElementById('testimonials-list');
    const footer = document.getElementById('testimonials-footer');
    const list = adminTestimonials.filter(t => tmFilter === 'ALL' || t.status === tmFilter);
    if (!list.length) {
        const msg = adminTestimonials.length ? 'No testimonials in this tab.' : 'No testimonials yet.';
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">${msg}</td></tr>`;
        footer.innerHTML = '<span class="pager-text">Showing 0 testimonials</span>';
        return;
    }
    tbody.innerHTML = list.map(t => {
        const st = TM_STATUS[t.status] || [t.status, 'pending'];
        return `
        <tr>
            <td>
                <div class="user-info">
                    <div class="user-name">${escapeHtml(t.name || '')}</div>
                    <div class="user-role">${escapeHtml(t.country || '')}</div>
                </div>
            </td>
            <td><span class="eg-stars">${'★'.repeat(t.rating)}<span class="off">${'★'.repeat(5 - t.rating)}</span></span></td>
            <td style="max-width:340px;white-space:normal;">${escapeHtml(t.message || '')}</td>
            <td>${oDate(t.createdAt)}</td>
            <td><span class="eg-status ${st[1]}">${st[0]}</span></td>
            <td>${t.reviewedByName ? escapeHtml(t.reviewedByName) : '—'}</td>
            <td>
                <div style="display:flex;gap:8px;">
                    ${t.status !== 'APPROVED' ? `<button class="action-btn" title="Approve" onclick="reviewTestimonial(${t.testimonialId}, 'approve')"><i class="bi bi-check-lg"></i></button>` : ''}
                    ${t.status !== 'REJECTED' ? `<button class="action-btn" title="Reject" onclick="reviewTestimonial(${t.testimonialId}, 'reject')"><i class="bi bi-x-lg"></i></button>` : ''}
                    <button class="action-btn delete" title="Delete" onclick="deleteTestimonial(${t.testimonialId})"><i class="bi bi-trash"></i></button>
                </div>
            </td>
        </tr>`;
    }).join('');
    footer.innerHTML = `<span class="pager-text">Showing ${list.length} of ${adminTestimonials.length} testimonials</span>`;
}

function reviewTestimonial(id, action) {
    const t = adminTestimonials.find(x => x.testimonialId === id);
    if (!t) return;
    const text = action === 'approve'
        ? `Approve the testimonial from <b>${escapeHtml(t.name)}</b>? It will be shown on the home page.`
        : `Reject the testimonial from <b>${escapeHtml(t.name)}</b>? It will not be shown on the website.`;
    showConfirm(text, async () => {
        const res = await apiFetch(`/admin/testimonials/${id}/${action}`, { method: 'PATCH' });
        if (res.ok) {
            showToast(action === 'approve' ? 'Testimonial approved.' : 'Testimonial rejected.');
            loadAdminTestimonials();
        } else if (res.status !== 401 && res.status !== 403) showToast(tjErr(res, 'Action failed.'), true);
    });
}

function deleteTestimonial(id) {
    showConfirm('Delete this testimonial permanently?', async () => {
        const res = await apiFetch(`/admin/testimonials/${id}`, { method: 'DELETE' });
        if (res.ok) { showToast('Testimonial deleted.'); loadAdminTestimonials(); }
        else if (res.status !== 401 && res.status !== 403) showToast(tjErr(res, 'Failed to delete.'), true);
    });
}

/* ======================= CONTACT MESSAGES ======================= */
const CONTACT_STATUSES = [['NEW', 'New'], ['READ', 'Read'], ['RESOLVED', 'Resolved']];
let adminContacts = [];

function showMessagesTab(tab) {
    document.getElementById('msg-tab-inquiries').style.display = tab === 'inquiries' ? 'block' : 'none';
    document.getElementById('msg-tab-contact').style.display = tab === 'contact' ? 'block' : 'none';
    document.getElementById('msgtab-inquiries').classList.toggle('active', tab === 'inquiries');
    document.getElementById('msgtab-contact').classList.toggle('active', tab === 'contact');
}

async function loadAdminContacts() {
    const res = await apiFetch('/admin/contact-messages');
    if (!res.ok || !Array.isArray(res.data)) {
        document.getElementById('contacts-list').innerHTML =
            '<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">Could not load contact messages.</td></tr>';
        return;
    }
    adminContacts = res.data;
    const wrap = document.getElementById('con-f-admin-wrap');
    if (wrap) wrap.style.display = isSuperAdmin() ? '' : 'none';
    fillFilter('con-f-admin', distinctSorted(adminContacts.map(c => c.handledByName)).map(v => [v, v]), 'All Admins');
    renderContacts();
}

function getFilteredContacts() {
    const q = fval('con-f-search').toLowerCase();
    const status = fval('con-f-status'), from = fval('con-f-from'), to = fval('con-f-to');
    const admin = isSuperAdmin() ? fval('con-f-admin') : '';
    return adminContacts.filter(c => {
        const day = (c.createdAt || '').slice(0, 10);
        return (!q || `${c.name} ${c.email} ${c.subject}`.toLowerCase().includes(q))
            && (!status || c.status === status)
            && (!from || day >= from)
            && (!to || day <= to)
            && (!admin || c.handledByName === admin);
    });
}

function contactFiltersChanged() { renderContacts(); }

function clearContactFilters() {
    ['con-f-search', 'con-f-status', 'con-f-from', 'con-f-to', 'con-f-admin']
        .forEach(id => { const el = document.getElementById(id); if (el) el.value = ''; });
    renderContacts();
}

async function refreshContacts() {
    await loadAdminContacts();
    showToast('Contact messages refreshed.');
}

function renderContacts() {
    const newCount = adminContacts.filter(c => c.status === 'NEW').length;
    const badge = document.getElementById('con-new-count');
    if (badge) { badge.textContent = newCount; badge.style.display = newCount ? 'inline-block' : 'none'; }

    const tbody = document.getElementById('contacts-list');
    const footer = document.getElementById('contacts-footer');
    const list = getFilteredContacts();
    if (!list.length) {
        const msg = adminContacts.length ? 'No messages match your filters.' : 'No contact messages yet.';
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:32px;color:var(--tj-text-muted);">${msg}</td></tr>`;
        footer.innerHTML = '<span class="pager-text">Showing 0 messages</span>';
        return;
    }
    tbody.innerHTML = list.map(c => `
        <tr class="${c.status === 'NEW' ? 'row-unread-msg' : ''}">
            <td>
                <div class="user-info">
                    <div class="user-name">${escapeHtml(c.name)}</div>
                    <div class="user-role">${escapeHtml(c.email)}</div>
                </div>
            </td>
            <td style="max-width:200px;">${escapeHtml(c.subject)}</td>
            <td style="max-width:260px;"><div style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:260px;color:var(--tj-text-muted);">${escapeHtml(c.message)}</div></td>
            <td>${oDate(c.createdAt)}</td>
            <td>
                <select class="status-select" data-prev="${c.status}" onchange="changeContactStatus(${c.messageId}, this)">
                    ${CONTACT_STATUSES.map(s => `<option value="${s[0]}" ${c.status === s[0] ? 'selected' : ''}>${s[1]}</option>`).join('')}
                </select>
            </td>
            <td>${c.handledByName ? escapeHtml(c.handledByName) : '—'}</td>
            <td>
                <div style="display:flex;gap:8px;">
                    <button class="action-btn" title="View / Reply" onclick="openContact(${c.messageId})"><i class="bi bi-envelope-open"></i></button>
                    <button class="action-btn delete" title="Delete" onclick="deleteContact(${c.messageId})"><i class="bi bi-trash"></i></button>
                </div>
            </td>
        </tr>`).join('');
    footer.innerHTML = `<span class="pager-text">Showing ${list.length === adminContacts.length ? list.length : list.length + ' of ' + adminContacts.length} messages</span>`;
}

function changeContactStatus(id, select) {
    const prev = select.dataset.prev, next = select.value;
    if (prev === next) return;
    const label = k => (CONTACT_STATUSES.find(x => x[0] === k) || [k, k])[1];
    showConfirm(
        `Change this message from “${label(prev)}” to “${label(next)}”?`,
        async () => {
            const res = await apiFetch(`/admin/contact-messages/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: next }) });
            if (res.ok) { showToast('Status updated.'); loadAdminContacts(); }
            else { select.value = prev; if (res.status !== 401 && res.status !== 403) showToast(tjErr(res, 'Failed to update status.'), true); }
        },
        () => { select.value = prev; }
    );
}

async function openContact(id) {
    const c = adminContacts.find(x => x.messageId === id);
    if (!c) return;
    document.getElementById('conModalTitle').textContent = c.subject;
    document.getElementById('conModalBody').innerHTML = `
        <div class="detail-grid">
            <div><span>From</span><strong>${escapeHtml(c.name)}</strong><small>${escapeHtml(c.email)}</small></div>
            <div><span>Received</span><strong>${oDateTime(c.createdAt)}</strong></div>
        </div>
        <div class="thread-bubble mine" style="margin-top:1rem;max-width:100%;">
            <div class="thread-text" style="white-space:pre-wrap;">${escapeHtml(c.message)}</div>
        </div>`;
    document.getElementById('conReplyLink').href =
        `mailto:${c.email.split('@').map(encodeURIComponent).join('@')}?subject=${encodeURIComponent('Re: ' + c.subject)}`;
    bootstrap.Modal.getOrCreateInstance(document.getElementById('contactModal')).show();

    // opening a new message marks it as read
    if (c.status === 'NEW') {
        const res = await apiFetch(`/admin/contact-messages/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: 'READ' }) });
        if (res.ok) loadAdminContacts();
    }
}

function deleteContact(id) {
    showConfirm('Delete this message permanently?', async () => {
        const res = await apiFetch(`/admin/contact-messages/${id}`, { method: 'DELETE' });
        if (res.ok) { showToast('Message deleted.'); loadAdminContacts(); }
        else if (res.status !== 401 && res.status !== 403) showToast(tjErr(res, 'Failed to delete.'), true);
    });
}

/* ======================= REPORTS (Super Admin) ======================= */
function repTable(headers, rows, empty) {
    if (!rows.length) return `<p class="text-muted">${empty || 'No data yet.'}</p>`;
    return `<div class="admin-card" style="padding:0;overflow:hidden;"><table class="admin-table">
        <thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead>
        <tbody>${rows.map(r => `<tr>${r.map(c => `<td>${c}</td>`).join('')}</tr>`).join('')}</tbody>
    </table></div>`;
}

function salesReportHtml(s) {
    const statusLabel = { PENDING: 'Pending', ADVANCE_PAID: 'Advance Paid', FULLY_PAID: 'Fully Paid', DELIVERED: 'Delivered' };
    const rows = list => list.map(r => [escapeHtml(r.label), r.count, oMoney(r.value)]);
    return `
    <div class="rep-block">
        <h5>Sales Summary</h5>
        <div class="rep-cards">
            <div class="rep-mini"><span>Total purchases</span><b>${s.totalPurchases}</b></div>
            <div class="rep-mini"><span>Total sales value</span><b>${oMoney(s.totalSalesValue)}</b></div>
            <div class="rep-mini"><span>Collected so far</span><b>${oMoney(s.totalCollected)}</b></div>
            <div class="rep-mini"><span>Balance due</span><b>${oMoney(s.balanceDue)}</b></div>
        </div>
        <div class="rep-two">
            <div><h6 class="text-tj-navy">By status</h6>
                ${repTable(['Status', 'Purchases'], Object.entries(s.byStatus).map(([k, v]) => [statusLabel[k] || k, v]))}</div>
            <div><h6 class="text-tj-navy">Top destinations</h6>
                ${repTable(['Country', 'Purchases', 'Value'], rows(s.topDestinations))}</div>
        </div>
        <div class="rep-two" style="margin-top:20px;">
            <div><h6 class="text-tj-navy">Top makes</h6>
                ${repTable(['Make', 'Purchases', 'Value'], rows(s.topMakes))}</div>
            <div><h6 class="text-tj-navy">Last 6 months</h6>
                ${repTable(['Month', 'Purchases', 'Value'], rows(s.monthly))}</div>
        </div>
    </div>`;
}

function activityReportHtml(a) {
    return `
    <div class="rep-block">
        <h5>Admin Activity</h5>
        ${repTable(['Admin', 'Role', 'Replies sent', 'Purchases processed', 'Testimonials reviewed', 'Contact messages handled'],
            a.admins.map(r => [`<strong>${escapeHtml(r.name)}</strong><div class="user-role">${escapeHtml(r.email)}</div>`,
                escapeHtml(r.role), r.repliesSent, r.purchasesProcessed, r.testimonialsReviewed, r.contactMessagesHandled]))}
        <h6 class="text-tj-navy" style="margin-top:20px;">Recent activity</h6>
        ${repTable(['When', 'Admin', 'Type', 'What they did'],
            a.recent.map(e => [oDateTime(e.time), escapeHtml(e.adminName), escapeHtml(e.type), escapeHtml(e.description)]),
            'No admin activity recorded yet.')}
    </div>`;
}

async function showReports(which) {
    showAdminSection('reports');
    const out = document.getElementById('rep-out');
    out.innerHTML = '<p class="text-muted">Loading...</p>';
    let html = '';
    if (which === 'sales' || which === 'all') {
        const res = await apiFetch('/admin/reports/sales-summary');
        html += res.ok && res.data ? salesReportHtml(res.data) : '<p class="text-muted">Could not load the sales summary.</p>';
    }
    if (which === 'activity' || which === 'all') {
        const res = await apiFetch('/admin/reports/admin-activity');
        html += res.ok && res.data ? activityReportHtml(res.data) : '<p class="text-muted">Could not load the admin activity.</p>';
    }
    out.innerHTML = html;
}

function loadReports()        { return showReports('all'); }
function loadSalesReport()    { return showReports('sales'); }
function loadActivityReport() { return showReports('activity'); }
