vehicles.js/* ================================================
   TRADE JAPAN — Inquiries & Purchases (customer side)
   Uses apiFetch / showConfirm / getToken from app.js
   ================================================ */

/* ---------- small helpers ---------- */
function oEsc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}
function oMoney(n) { return '$' + Number(n || 0).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 }); }
function oDate(d) { return d ? new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: '2-digit' }) : '—'; }
function oDateTime(d) { return d ? new Date(d).toLocaleString('en-US', { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''; }

/* error text from an apiFetch result ({message} or {field: message}) */
function tjErr(res, fallback) {
    const d = res && res.data;
    if (d && typeof d === 'object') {
        if (d.message) return d.message;
        const first = Object.values(d)[0];
        if (first) return String(first);
    }
    if (typeof d === 'string' && d.trim()) return d;
    return fallback;
}

/* ---------- Countries (used by the inquiry form and the admin "Add Purchase" form) ---------- */
const TJ_COUNTRIES = ['Afghanistan','Albania','Algeria','Angola','Argentina','Armenia','Australia','Austria','Azerbaijan','Bahamas','Bahrain','Bangladesh','Barbados','Belarus','Belgium','Belize','Benin','Bhutan','Bolivia','Bosnia and Herzegovina','Botswana','Brazil','Brunei','Bulgaria','Burkina Faso','Burundi','Cambodia','Cameroon','Canada','Cape Verde','Central African Republic','Chad','Chile','China','Colombia','Comoros','Congo','Costa Rica','Croatia','Cuba','Cyprus','Czech Republic','Denmark','Djibouti','Dominican Republic','DR Congo','Ecuador','Egypt','El Salvador','Equatorial Guinea','Eritrea','Estonia','Eswatini','Ethiopia','Fiji','Finland','France','Gabon','Gambia','Georgia','Germany','Ghana','Greece','Guatemala','Guinea','Guyana','Haiti','Honduras','Hong Kong','Hungary','Iceland','India','Indonesia','Iran','Iraq','Ireland','Israel','Italy','Ivory Coast','Jamaica','Jordan','Kazakhstan','Kenya','Kuwait','Kyrgyzstan','Laos','Latvia','Lebanon','Lesotho','Liberia','Libya','Lithuania','Luxembourg','Madagascar','Malawi','Malaysia','Maldives','Mali','Malta','Mauritania','Mauritius','Mexico','Moldova','Mongolia','Montenegro','Morocco','Mozambique','Myanmar','Namibia','Nepal','Netherlands','New Zealand','Nicaragua','Niger','Nigeria','North Macedonia','Norway','Oman','Pakistan','Palestine','Panama','Papua New Guinea','Paraguay','Peru','Philippines','Poland','Portugal','Qatar','Romania','Russia','Rwanda','Samoa','Saudi Arabia','Senegal','Serbia','Seychelles','Sierra Leone','Singapore','Slovakia','Slovenia','Solomon Islands','Somalia','South Africa','South Korea','South Sudan','Spain','Sri Lanka','Sudan','Suriname','Sweden','Switzerland','Syria','Taiwan','Tajikistan','Tanzania','Thailand','Timor-Leste','Togo','Tonga','Trinidad and Tobago','Tunisia','Turkey','Turkmenistan','Uganda','Ukraine','United Arab Emirates','United Kingdom','United States','Uruguay','Uzbekistan','Vanuatu','Venezuela','Vietnam','Yemen','Zambia','Zimbabwe'];

function countryOptionsHtml(selected) {
    return '<option value="">Select country...</option>' +
        TJ_COUNTRIES.map(c => `<option value="${oEsc(c)}"${c === selected ? ' selected' : ''}>${oEsc(c)}</option>`).join('');
}

function tjToast(message, isError = false) {
    const toast = document.createElement('div');
    toast.style.cssText = `position:fixed;bottom:24px;right:24px;padding:14px 24px;border-radius:8px;color:#fff;font-size:0.95rem;font-weight:500;z-index:9999;box-shadow:0 4px 16px rgba(0,0,0,0.15);transition:opacity .3s;max-width:90vw;background:${isError ? 'var(--tj-danger)' : 'var(--tj-success)'};`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 3500);
}

function emptyCard(icon, text) {
    return `<div class="admin-card" style="text-align:center;color:var(--tj-text-muted);padding:48px;">
                <i class="bi ${icon}" style="font-size:2.5rem;"></i>
                <p style="margin-top:12px;">${text}</p>
            </div>`;
}

/* ---------- Send Inquiry (vehicle detail page) ---------- */
function openInquiryModal(btn) {
    const vehicleId = Number(btn.dataset.vid);
    const vehicleName = btn.dataset.vname || 'this vehicle';

    if (!getToken()) {
        showConfirm('Please log in to send an inquiry about this vehicle.', () => { window.location.href = '/pages/login.html'; });
        return;
    }

    document.getElementById('tj-confirm-overlay')?.remove();
    const overlay = document.createElement('div');
    overlay.id = 'tj-confirm-overlay';
    overlay.className = 'tj-confirm-overlay';
    overlay.innerHTML = `
        <div class="tj-confirm-box" style="max-width:520px;text-align:left;">
            <p class="tj-confirm-message" style="margin-bottom:.4rem;">Send Inquiry</p>
            <p style="text-align:center;color:var(--tj-text-muted);margin-bottom:1.4rem;">${oEsc(vehicleName)}</p>
            <div class="tj-form-group" style="text-align:left;">
                <label>Subject <span class="required">*</span></label>
                <div class="tj-input-icon">
                    <i class="bi bi-chat-left-text input-icon-left"></i>
                    <input type="text" id="inq-subject" maxlength="150" value="Inquiry about ${oEsc(vehicleName)}">
                </div>
            </div>
            <div class="tj-form-group" style="text-align:left;">
                <label>Destination Country <span class="required">*</span></label>
                <select id="inq-country" class="tj-select">${countryOptionsHtml()}</select>
            </div>
            <div class="tj-form-group" style="text-align:left;">
                <label>Message <span class="required">*</span></label>
                <textarea id="inq-message" class="tj-textarea" rows="4" maxlength="2000"
                          placeholder="Ask about price, shipping, condition, availability..."></textarea>
            </div>
            <div id="inq-error" class="auth-message" style="text-align:left;min-height:20px;"></div>
            <div class="tj-confirm-actions">
                <button class="btn-tj-outline" id="inq-cancel">Cancel</button>
                <button class="btn-tj-primary" id="inq-send">Send Inquiry</button>
            </div>
        </div>`;
    document.body.appendChild(overlay);
    document.getElementById('inq-country').focus();

    document.getElementById('inq-cancel').onclick = () => overlay.remove();
    document.getElementById('inq-send').onclick = async () => {
        const err = document.getElementById('inq-error');
        const subject = document.getElementById('inq-subject').value.trim();
        const country = document.getElementById('inq-country').value.trim();
        const message = document.getElementById('inq-message').value.trim();
        if (!subject || !country || !message) { err.innerText = 'Please fill in all fields.'; return; }
        const btnSend = document.getElementById('inq-send');
        btnSend.disabled = true;
        const res = await apiFetch('/inquiries', {
            method: 'POST',
            body: JSON.stringify({ vehicleId, subject, message, destinationCountry: country })
        });
        btnSend.disabled = false;
        if (res.ok) {
            overlay.remove();
            tjToast('Inquiry sent! Track replies in My Account → Message.');
        } else if (res.status !== 401) {
            err.innerText = tjErr(res, 'Could not send inquiry. Please try again.');
        }
    };
}

/* ---------- My Account: Messages tab ---------- */
function inquiryBadge(status) {
    switch (status) {
        case 'CONTACTED':   return ['badge-answered', 'Answered'];
        case 'CLOSED':      return ['badge-answered', 'Closed'];
        case 'IN_PROGRESS': return ['badge-pending', 'In Progress'];
        default:            return ['badge-pending', 'Pending'];
    }
}

function inquiryItemHtml(i) {
    const [cls, label] = inquiryBadge(i.status);
    const img = i.vehicleImage ? `<img src="${oEsc(i.vehicleImage)}" alt="">` : '<i class="bi bi-car-front"></i>';
    const replies = i.replyCount > 0 ? `${i.replyCount} repl${i.replyCount === 1 ? 'y' : 'ies'}` : 'No replies yet';
    return `
        <div class="inq-wrap">
            <div class="message-item inq-clickable" onclick="toggleInquiryThread(${i.inquiryId})">
                <div class="msg-image">${img}</div>
                <div class="msg-content">
                    <h4>${oEsc(i.subject)}</h4>
                    <p><strong>${oEsc(i.vehicleName)}</strong> &middot; ${oEsc(i.message)}</p>
                </div>
                <div class="msg-meta">
                    <span class="${cls}">${label}</span>
                    <div class="msg-date">${oDate(i.createdAt)}</div>
                    <div class="msg-replies">${replies}</div>
                </div>
            </div>
            <div class="msg-thread" id="thread-${i.inquiryId}" style="display:none;"
                 data-loaded="0" data-message="${oEsc(i.message)}" data-country="${oEsc(i.destinationCountry)}" data-date="${oEsc(i.createdAt)}"></div>
        </div>`;
}

async function loadMyInquiries() {
    const box = document.getElementById('tab-messages');
    if (!box) return;
    box.innerHTML = emptyCard('bi-arrow-clockwise', 'Loading your messages...');
    const res = await apiFetch('/inquiries/me');
    if (!res.ok || !Array.isArray(res.data)) { box.innerHTML = emptyCard('bi-exclamation-circle', 'Could not load your messages.'); return; }
    if (!res.data.length) {
        box.innerHTML = emptyCard('bi-chat-square-text', 'No messages yet. Open a vehicle and click “Send Inquiry”.');
        return;
    }
    box.innerHTML = res.data.map(inquiryItemHtml).join('');
}

/* a chat bubble: the customer's own messages on the right ("You"), the team's on the left */
function customerBubbleHtml(r) {
    if (r.fromAdmin) {
        return `
            <div class="thread-bubble admin">
                <div class="thread-author">${oEsc(r.authorName)} &middot; ${oDateTime(r.createdAt)}</div>
                <div class="thread-text">${oEsc(r.message)}</div>
            </div>`;
    }
    return `
        <div class="thread-bubble mine">
            <div class="thread-author">You &middot; ${oDateTime(r.createdAt)}</div>
            <div class="thread-text">${oEsc(r.message)}</div>
        </div>`;
}

async function renderCustomerThread(id) {
    const el = document.getElementById('thread-' + id);
    if (!el) return;
    if (el.dataset.loaded !== '1') el.innerHTML = '<div class="thread-note">Loading conversation...</div>';

    const res = await apiFetch(`/inquiries/${id}/replies`);
    const replies = (res.ok && Array.isArray(res.data)) ? res.data : [];
    const draft = document.getElementById('cust-reply-' + id)?.value || '';

    el.innerHTML = `
        <div class="thread-bubble mine">
            <div class="thread-author">You &middot; ${oDateTime(el.dataset.date)}</div>
            <div class="thread-text">${oEsc(el.dataset.message)}</div>
            <div class="thread-author" style="margin-top:.5rem;">Destination: ${oEsc(el.dataset.country)}</div>
        </div>
        ${replies.map(customerBubbleHtml).join('')}
        ${replies.some(r => r.fromAdmin) ? '' : '<div class="thread-note">No reply yet — we will get back to you soon.</div>'}
        <div class="thread-compose">
            <textarea id="cust-reply-${id}" class="tj-textarea" rows="2" maxlength="2000" placeholder="Write a message..."></textarea>
            <button class="btn-tj-primary" onclick="sendCustomerReply(${id}, this)"><i class="bi bi-send"></i> Send</button>
        </div>
        <div class="thread-error" id="cust-reply-err-${id}"></div>`;
    document.getElementById('cust-reply-' + id).value = draft;
    el.dataset.loaded = '1';
}

async function toggleInquiryThread(id) {
    const el = document.getElementById('thread-' + id);
    if (!el) return;
    if (el.style.display === 'block') { el.style.display = 'none'; return; }
    el.style.display = 'block';
    await renderCustomerThread(id);   // always reload so new replies from the team show up
}

/* the customer can write as many messages as they like */
async function sendCustomerReply(id, btn) {
    const box = document.getElementById('cust-reply-' + id);
    const err = document.getElementById('cust-reply-err-' + id);
    const message = box.value.trim();
    if (!message) { err.textContent = 'Please type a message.'; return; }
    err.textContent = '';
    btn.disabled = true;
    const res = await apiFetch(`/inquiries/${id}/replies`, { method: 'POST', body: JSON.stringify({ message }) });
    btn.disabled = false;
    if (res.ok) {
        box.value = '';
        await renderCustomerThread(id);
    } else if (res.status !== 401) {
        err.textContent = tjErr(res, 'Could not send your message. Please try again.');
    }
}

/* ---------- My Account: Purchase History tab ---------- */
const PURCHASE_STEPS = [
    ['PENDING', 'Pending'], ['ADVANCE_PAID', 'Advance Paid'], ['FULLY_PAID', 'Fully Paid'], ['DELIVERED', 'Delivered']
];

function purchaseStepsHtml(status) {
    const current = PURCHASE_STEPS.findIndex(s => s[0] === status);
    return `<div class="p-steps">${PURCHASE_STEPS.map((s, i) =>
        `<div class="p-step ${i <= current ? 'done' : ''} ${i === current ? 'current' : ''}"><span class="p-dot"></span><span>${s[1]}</span></div>`).join('')}</div>`;
}

function purchaseStatusPill(status) {
    const label = (PURCHASE_STEPS.find(s => s[0] === status) || [0, status])[1];
    return `<span class="p-status p-status-${String(status).toLowerCase().replace('_', '-')}">${label}</span>`;
}

function purchaseCardHtml(p) {
    const img = p.vehicleImage ? `<img src="${oEsc(p.vehicleImage)}" alt="">` : '<i class="bi bi-car-front"></i>';
    return `
        <div class="admin-card purchase-card">
            <div class="purchase-top">
                <div class="msg-image purchase-img">${img}</div>
                <div class="purchase-title">
                    <h4>${oEsc(p.vehicleName)}</h4>
                    <div class="purchase-sub">Purchase #${p.purchaseId} &middot; ${oDate(p.createdAt)}</div>
                </div>
                ${purchaseStatusPill(p.status)}
            </div>
            ${purchaseStepsHtml(p.status)}
            <div class="purchase-amounts">
                <div><span>Total Price</span><strong>${oMoney(p.totalPrice)}</strong></div>
                <div><span>Advance Paid</span><strong>${oMoney(p.advancePaid)}</strong></div>
                <div><span>Balance</span><strong class="${Number(p.balance) > 0 ? 'text-due' : ''}">${oMoney(p.balance)}</strong></div>
                <div><span>Invoice</span><strong>${p.invoiceNo ? oEsc(p.invoiceNo) : '—'}</strong></div>
            </div>
            ${p.notes ? `<div class="purchase-notes"><i class="bi bi-info-circle"></i> ${oEsc(p.notes)}</div>` : ''}
            ${p.invoiceFileUrl ? `<button class="btn-tj-outline" onclick="downloadInvoice(${p.purchaseId})"><i class="bi bi-file-earmark-pdf"></i> Download Invoice</button>` : ''}
        </div>`;
}

async function loadMyPurchases() {
    const box = document.getElementById('tab-history');
    if (!box) return;
    box.innerHTML = emptyCard('bi-arrow-clockwise', 'Loading your purchases...');
    const res = await apiFetch('/purchases/me');
    if (!res.ok || !Array.isArray(res.data)) { box.innerHTML = emptyCard('bi-exclamation-circle', 'Could not load your purchases.'); return; }
    if (!res.data.length) { box.innerHTML = emptyCard('bi-bag', 'No purchase history yet.'); return; }
    box.innerHTML = res.data.map(purchaseCardHtml).join('');
}

/* ---------- Invoice download (needs the login token, so a plain link will not work) ---------- */
async function downloadInvoice(id) {
    const token = getToken();
    const response = await fetch(`/api/purchases/${id}/invoice`, { headers: token ? { 'Authorization': 'Bearer ' + token } : {} });
    if (!response.ok) {
        let msg = 'Could not download the invoice.';
        try { const j = await response.json(); if (j.message) msg = j.message; } catch (e) { /* not json */ }
        tjToast(msg, true);
        return;
    }
    const blob = await response.blob();
    const cd = response.headers.get('Content-Disposition') || '';
    const m = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(cd);
    const name = m ? decodeURIComponent(m[1]) : `invoice-${id}.pdf`;
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(a.href), 1000);
}

/* ---------- My Account: Wishlist tab (saved vehicles) ---------- */
function wishlistCardHtml(v) {
    const img = (v.imageUrls || '').split(',')[0].trim();
    return `
    <div class="wl-card" id="wl-${v.id}">
        <a class="wl-link" href="/pages/vehicle-detail.html?id=${v.id}">
            <div class="wl-img">${img ? `<img src="${oEsc(img)}" alt="" loading="lazy">` : '<i class="bi bi-car-front"></i>'}</div>
            <div class="wl-info">
                <div class="wl-name">${oEsc(v.make)} ${oEsc(v.model)}</div>
                <div class="wl-meta">${oEsc(v.manufactureYear)} &nbsp;|&nbsp; ${oMoney(v.fobPrice)}</div>
            </div>
        </a>
        <button class="wl-remove" title="Remove from wishlist" onclick="removeFromWishlist(${v.id})"><i class="bi bi-heart-fill"></i></button>
    </div>`;
}

async function loadMyWishlist() {
    const box = document.getElementById('tab-wishlist');
    if (!box) return;
    box.innerHTML = emptyCard('bi-arrow-clockwise', 'Loading your saved vehicles...');
    const res = await apiFetch('/wishlist');
    if (!res.ok || !Array.isArray(res.data)) {
        box.innerHTML = emptyCard('bi-exclamation-circle', 'Could not load your saved vehicles.');
        return;
    }
    if (!res.data.length) {
        box.innerHTML = emptyCard('bi-heart', 'Your wishlist is empty. Press "Save Vehicle" on any vehicle page to add it here.');
        return;
    }
    box.innerHTML = `<div class="wl-grid">${res.data.map(wishlistCardHtml).join('')}</div>`;
}

async function removeFromWishlist(id) {
    const res = await apiFetch('/wishlist/' + id, { method: 'DELETE' });
    if (res.ok) {
        document.getElementById('wl-' + id)?.remove();
        if (!document.querySelector('.wl-card')) loadMyWishlist();
        tjToast('Removed from your wishlist.');
    } else if (res.status !== 401) {
        tjToast('Could not remove it. Please try again.', true);
    }
}
