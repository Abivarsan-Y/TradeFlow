/* ================================================
   TRADE JAPAN — Customer side: Contact Us + Testimonials
   - home page: approved testimonials
   - pages/contact.html: Contact Us form (no login needed)
   - pages/submit-testimonial.html: testimonial form (login needed) + "my testimonials"
   Uses apiFetch / getToken from app.js. The testimonial page also loads orders.js (TJ_COUNTRIES).
   ================================================ */

const egEsc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[c]));

/* message from an API error: {message:"..."} or a field-error map {field:"..."} */
function egErr(res, fallback) {
    const d = res && res.data;
    if (d && typeof d === 'object') {
        if (d.message) return d.message;
        const first = Object.values(d)[0];
        if (first) return String(first);
    }
    return fallback;
}

function egStars(n) {
    n = Math.max(0, Math.min(5, Number(n) || 0));
    return `<span class="eg-stars" aria-label="${n} out of 5">${'★'.repeat(n)}<span class="off">${'★'.repeat(5 - n)}</span></span>`;
}

function egInitials(name) {
    return (name || 'TJ').trim().split(/\s+/).slice(0, 2).map(x => x[0] || '').join('').toUpperCase();
}

function egNotice(el, message, type) {
    if (!el) return;
    el.textContent = message;
    el.className = 'eg-notice show ' + (type || 'success');
}

function egDate(d) {
    return d ? new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: '2-digit' }) : '';
}

/* a name like "Kamal S." from the profile, so the customer does not have to type it */
async function egPrefillFromProfile() {
    if (!getToken()) return null;
    const res = await apiFetch('/profile');
    return res.ok && res.data && typeof res.data === 'object' ? res.data : null;
}

/* ---------------- HOME PAGE: approved testimonials ---------------- */
async function loadHomeTestimonials() {
    const box = document.getElementById('testimonial-grid');
    if (!box) return;
    const res = await apiFetch('/testimonials/approved');
    const list = res.ok && Array.isArray(res.data) ? res.data : [];
    if (!list.length) {
        box.innerHTML = `
            <div class="vehicle-empty">
                <i class="bi bi-chat-quote"></i> No testimonials yet. Be the first to share your experience!
            </div>`;
        return;
    }
    box.innerHTML = list.slice(0, 6).map(t => `
        <article class="testimonial-card">
            <div class="tc-profile">
                <div class="tc-avatar">${egEsc(egInitials(t.name))}</div>
                <div>
                    <strong>${egEsc(t.name)}</strong>
                    <span>${egEsc(t.country || 'International customer')}</span>
                </div>
            </div>
            ${egStars(t.rating)}
            <p class="tc-quote">&ldquo;${egEsc(t.message)}&rdquo;</p>
        </article>`).join('');
}

/* ---------------- CONTACT US PAGE ---------------- */
async function initContactPage() {
    const form = document.getElementById('contactForm');
    if (!form) return;
    const notice = document.getElementById('contactNotice');

    const me = await egPrefillFromProfile();
    if (me) {
        form.name.value = ((me.firstName || '') + ' ' + (me.lastName || '')).trim();
        form.email.value = me.email || '';
    }

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const data = {
            name: form.name.value.trim(),
            email: form.email.value.trim(),
            subject: form.subject.value.trim(),
            message: form.message.value.trim()
        };
        if (!data.name || !data.email || !data.subject || !data.message) {
            egNotice(notice, 'Please fill in all the required fields.', 'error');
            return;
        }
        const btn = form.querySelector('button[type="submit"]');
        btn.disabled = true;
        const res = await apiFetch('/contact-messages', { method: 'POST', body: JSON.stringify(data) });
        btn.disabled = false;
        if (res.ok) {
            form.message.value = '';
            form.subject.value = '';
            egNotice(notice, 'Thank you! Your message has been sent. Our team will get back to you soon.', 'success');
        } else {
            egNotice(notice, egErr(res, 'Could not send your message. Please try again.'), 'error');
        }
    });
}

/* ---------------- SUBMIT TESTIMONIAL PAGE ---------------- */
const EG_STATUS = {
    PENDING:  ['Waiting for review', 'pending'],
    APPROVED: ['Published', 'approved'],
    REJECTED: ['Not published', 'rejected']
};

async function initTestimonialPage() {
    const form = document.getElementById('testimonialForm');
    if (!form) return;

    // Only logged-in customers can write a testimonial
    if (!getToken()) {
        document.getElementById('tm-login').style.display = 'block';
        document.getElementById('tm-form-wrap').style.display = 'none';
        return;
    }
    document.getElementById('tm-login').style.display = 'none';
    document.getElementById('tm-form-wrap').style.display = 'block';

    const notice = document.getElementById('testimonialNotice');
    const countrySel = document.getElementById('tm-country');
    countrySel.innerHTML = countryOptionsHtml();

    const me = await egPrefillFromProfile();
    if (me) {
        const last = (me.lastName || '').trim();
        form.name.value = ((me.firstName || '').trim() + (last ? ' ' + last[0] + '.' : '')).trim();
    }

    // star rating
    let rating = 0;
    const starBtns = [...document.querySelectorAll('#tm-rating button')];
    const paint = n => starBtns.forEach(b => b.classList.toggle('on', Number(b.dataset.v) <= n));
    starBtns.forEach(b => {
        b.addEventListener('click', () => { rating = Number(b.dataset.v); paint(rating); });
        b.addEventListener('mouseenter', () => paint(Number(b.dataset.v)));
        b.addEventListener('mouseleave', () => paint(rating));
    });

    // character counter
    const msg = form.message, counter = document.getElementById('tm-count');
    const update = () => { counter.textContent = msg.value.length + ' / 1000'; };
    msg.addEventListener('input', update); update();

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const data = {
            name: form.name.value.trim(),
            country: countrySel.value,
            rating: rating,
            message: msg.value.trim()
        };
        if (!data.name || !data.country || !data.rating || !data.message) {
            egNotice(notice, 'Please complete every field and choose a rating.', 'error');
            return;
        }
        if (data.message.length < 10) {
            egNotice(notice, 'Your testimonial must be at least 10 characters.', 'error');
            return;
        }
        const btn = form.querySelector('button[type="submit"]');
        btn.disabled = true;
        const res = await apiFetch('/testimonials', { method: 'POST', body: JSON.stringify(data) });
        btn.disabled = false;
        if (res.ok) {
            msg.value = ''; update();
            rating = 0; paint(0);
            egNotice(notice, 'Thank you! Your testimonial was submitted and will appear on the website once our team approves it.', 'success');
            loadMyTestimonials();
        } else if (res.status !== 401) {
            egNotice(notice, egErr(res, 'Could not submit your testimonial. Please try again.'), 'error');
        }
    });

    loadMyTestimonials();
}

async function loadMyTestimonials() {
    const box = document.getElementById('tm-mine');
    if (!box) return;
    const res = await apiFetch('/testimonials/me');
    const list = res.ok && Array.isArray(res.data) ? res.data : [];
    if (!list.length) { box.innerHTML = ''; return; }
    box.innerHTML = `
        <h3 class="eg-sub">Your testimonials</h3>
        ${list.map(t => {
            const st = EG_STATUS[t.status] || [t.status, 'pending'];
            return `
            <div class="eg-mine-item">
                <div class="eg-mine-top">
                    ${egStars(t.rating)}
                    <span class="eg-status ${st[1]}">${st[0]}</span>
                </div>
                <p>&ldquo;${egEsc(t.message)}&rdquo;</p>
                <small>${egEsc(t.country || '')} &middot; ${egDate(t.createdAt)}</small>
            </div>`;
        }).join('')}`;
}

document.addEventListener('DOMContentLoaded', () => {
    loadHomeTestimonials();
    initContactPage();
    initTestimonialPage();
});
