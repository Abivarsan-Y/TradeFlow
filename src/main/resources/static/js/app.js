/* ================================================
   TRADE JAPAN CMS — Global JavaScript
   ================================================ */

const API_URL = '/api';

/* ---------- Auth Helpers ---------- */
function getToken()       { return localStorage.getItem('jwt_token'); }
function setToken(token)  { localStorage.setItem('jwt_token', token); }
function getRole()        { return localStorage.getItem('user_role'); }
function setRole(role)    { localStorage.setItem('user_role', role); }
function getUserName()    { return localStorage.getItem('user_name') || ''; }
function setUserName(n)   { localStorage.setItem('user_name', n); }

function logout() {
    showConfirm('Are you sure you want to log out?', () => {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_role');
        localStorage.removeItem('user_name');
        window.location.href = '/';
    });
}

/* ---------- Reusable Confirmation Modal ---------- */
function showConfirm(message, onConfirm, onCancel) {
    // Remove any existing confirm modal first, so they never stack
    document.getElementById('tj-confirm-overlay')?.remove();

    const overlay = document.createElement('div');
    overlay.id = 'tj-confirm-overlay';
    overlay.className = 'tj-confirm-overlay';
    overlay.innerHTML = `
        <div class="tj-confirm-box">
            <p class="tj-confirm-message">${message}</p>
            <div class="tj-confirm-actions">
                <button class="btn-tj-outline" id="tj-confirm-cancel">Cancel</button>
                <button class="btn-tj-primary" id="tj-confirm-ok">Confirm</button>
            </div>
        </div>`;
    document.body.appendChild(overlay);

    document.getElementById('tj-confirm-cancel').onclick = () => {
        overlay.remove();
        if (onCancel) onCancel();
    };
    document.getElementById('tj-confirm-ok').onclick = () => {
        overlay.remove();
        onConfirm();
    };
}

function showPasswordPrompt(message, onSubmit) {
    document.getElementById('tj-confirm-overlay')?.remove();

    const overlay = document.createElement('div');
    overlay.id = 'tj-confirm-overlay';
    overlay.className = 'tj-confirm-overlay';
    overlay.innerHTML = `
        <div class="tj-confirm-box" style="max-width:400px;">
            <p class="tj-confirm-message">${message}</p>
            <div class="tj-form-group" style="text-align:left;">
                <label>New Password</label>
                <div class="tj-input-icon">
                    <i class="bi bi-lock input-icon-left"></i>
                    <input type="password" id="tj-prompt-input" placeholder="Min 8 characters">
                    <button type="button" class="input-icon-right" onclick="togglePassword('tj-prompt-input')"><i class="bi bi-eye"></i></button>
                </div>
            </div>
            <div class="tj-form-group" style="text-align:left;">
                <label>Confirm Password</label>
                <div class="tj-input-icon">
                    <i class="bi bi-lock input-icon-left"></i>
                    <input type="password" id="tj-prompt-confirm" placeholder="Re-enter password">
                    <button type="button" class="input-icon-right" onclick="togglePassword('tj-prompt-confirm')"><i class="bi bi-eye"></i></button>
                </div>
            </div>
            <div id="tj-prompt-error" class="auth-message" style="text-align:left;min-height:20px;"></div>
            <div class="tj-confirm-actions">
                <button class="btn-tj-outline" id="tj-prompt-cancel">Cancel</button>
                <button class="btn-tj-primary" id="tj-prompt-ok">Reset Password</button>
            </div>
        </div>`;
    document.body.appendChild(overlay);

    const input = document.getElementById('tj-prompt-input');
    const confirmInput = document.getElementById('tj-prompt-confirm');
    const err = document.getElementById('tj-prompt-error');
    input.focus();

    const submit = async () => {
        if (input.value.length < 8) {
            err.innerText = 'Password must be at least 8 characters.';
            return;
        }
        if (input.value !== confirmInput.value) {
            err.innerText = 'Passwords do not match.';
            return;
        }
        overlay.remove();
        await onSubmit(input.value);
    };

    document.getElementById('tj-prompt-cancel').onclick = () => overlay.remove();
    document.getElementById('tj-prompt-ok').onclick = submit;
    [input, confirmInput].forEach(el =>
        el.addEventListener('keydown', e => { if (e.key === 'Enter') submit(); })
    );
}

/* ---------- API Fetch ---------- */
let sessionExpiredHandled = false;

async function apiFetch(url, options = {}) {
    if (!options.headers) options.headers = {};
    const token = getToken();
    if (token) options.headers['Authorization'] = `Bearer ${token}`;
    if (!options.headers['Content-Type'] && !(options.body instanceof FormData)) {
        options.headers['Content-Type'] = 'application/json';
    }
    const response = await fetch(API_URL + url, options);

    // Login, register and password-reset live under /auth/. A 401 there just
    // means "wrong credentials", not "session expired", so it is handled by the page.
    if (response.status === 401 && !url.startsWith('/auth/')) {
        if (!sessionExpiredHandled) {
            sessionExpiredHandled = true;
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user_role');
            localStorage.removeItem('user_name');
            const isAdminArea = window.location.pathname.startsWith('/pages/admin/');
            window.location.href = isAdminArea ? '/pages/admin/login.html' : '/pages/login.html';
        }
        return { ok: false, status: 401, data: null };
    }

    if (response.status === 403 && !url.startsWith('/auth/')) {
        if (typeof showToast === 'function') {
            showToast("You don't have permission to perform this action.", true);
        }
        return { ok: false, status: 403, data: null };
    }

    const data = await response.text();
    try   { return { ok: response.ok, status: response.status, data: JSON.parse(data) }; }
    catch { return { ok: response.ok, status: response.status, data: data }; }
}

/* ---------- Render Public Navbar ---------- */
function renderNavbar() {
    const el = document.getElementById('navbar');
    if (!el) return;
    const token = getToken();
    const role  = getRole();

    let rightIcons = '';
    if (token) {
        rightIcons = `
            <a href="/pages/profile.html" title="My Account"><i class="bi bi-person"></i></a>
            ${(role === 'ROLE_ADMIN' || role === 'ROLE_SUPER_ADMIN')
                ? '<a href="/pages/admin/dashboard.html" title="Admin"><i class="bi bi-gear"></i></a>' : ''}
            <button onclick="logout()" title="Logout"><i class="bi bi-box-arrow-right"></i></button>`;
    } else {
        rightIcons = `
            <a href="/pages/login.html" title="Login"><i class="bi bi-person"></i></a>`;
    }

    el.innerHTML = `
        <nav class="tj-navbar">
            <button class="navbar-hamburger" onclick="toggleMobileMenu()">
                <i class="bi bi-list"></i>
            </button>
            <a href="/" class="navbar-logo">
                <div>
                    <div class="logo-text"><span class="trade">TRADE</span> <span class="japan">JAPAN</span></div>
                    <div class="logo-tagline">MAKE EVERY MILES COUNT</div>
                </div>
                <i class="bi bi-car-front logo-car"></i>
            </a>
            <div class="navbar-icons">
                                <a href="#" title="Search" onclick="toggleSearch();return false;"><i class="bi bi-search"></i></a>
                <a href="#" title="Cart"><i class="bi bi-cart3"></i></a>
                ${rightIcons}
            </div>
        </nav>
        <div class="search-panel" id="search-panel">
            <div class="search-box">
                <i class="bi bi-search"></i>
                <input type="text" id="search-input" placeholder="Search About, Services, FAQ..."
                       onkeydown="if(event.key==='Enter')runSearch();if(event.key==='Escape')toggleSearch();">
                <button onclick="runSearch()">Search</button>
                <button class="search-close" onclick="toggleSearch()"><i class="bi bi-x-lg"></i></button>
            </div>
            <div id="search-results"></div>
        </div>
        <div class="mobile-menu-overlay" onclick="toggleMobileMenu()"></div>
                <div class="mobile-menu">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;">
                <span style="font-weight:700;font-size:1.1rem;"><span style="color:var(--tj-maroon)">TRADE</span> JAPAN</span>
                <button onclick="toggleMobileMenu()" style="background:none;border:none;font-size:1.4rem;cursor:pointer;"><i class="bi bi-x-lg"></i></button>
            </div>
            <div class="menu-links-top">
                <a class="menu-link" href="/">Home</a>
                <a class="menu-link" href="/pages/content.html?page=about">About Us</a>
                <a class="menu-link" href="/pages/content.html?page=services">Services</a>
                <a class="menu-link" href="/pages/faq.html">FAQ</a>
            </div>
            <div class="menu-links-bottom">
                ${token
        ? `<a class="menu-link" href="/pages/profile.html">My Account</a>
                       <a class="menu-link" href="#" onclick="logout()" style="color:var(--tj-maroon)">Logout</a>`
        : `<a class="menu-link" href="/pages/login.html">Login</a>
                       <a class="menu-link" href="/pages/register.html">Register</a>`
    }
            </div>
        </div>`;
}

/* ---------- Toggle Mobile Menu ---------- */
function toggleMobileMenu() {
    document.querySelector('.mobile-menu')?.classList.toggle('open');
    document.querySelector('.mobile-menu-overlay')?.classList.toggle('open');
}

/* ---------- Render Public Footer ---------- */
function renderFooter() {
    const el = document.getElementById('footer');
    if (!el) return;
    el.innerHTML = `
        <footer class="tj-footer">
            <div class="footer-top">
                <div class="footer-logo">
                    <div class="logo-text"><span class="trade">TRADE</span> JAPAN</div>
                    <div class="logo-tagline">MAKE EVERY MILES COUNT</div>
                    <div class="footer-social">
                        <a href="#"><i class="bi bi-facebook"></i></a>
                        <a href="#"><i class="bi bi-instagram"></i></a>
                        <a href="#"><i class="bi bi-youtube"></i></a>
                        <a href="#"><i class="bi bi-linkedin"></i></a>
                    </div>
                </div>
                <div>
                    <h6>Company</h6>
                    <ul>
                        <li><a href="/pages/content.html?page=about">About Us</a></li>
                        <li><a href="/pages/content.html?page=tc">Terms &amp; Conditions</a></li>
                        <li><a href="/pages/content.html?page=privacy">Privacy Policy</a></li>
                        <li><a href="#">Contact Us</a></li>
                    </ul>
                </div>
                <div>
                    <h6>Information</h6>
                    <ul>
                        <li><a href="/pages/faq.html">FAQ</a></li>
                        <li><a href="#">How To Buy</a></li>
                        <li><a href="#">Auction Service</a></li>
                        <li><a href="/pages/content.html?page=services">Services</a></li>
                    </ul>
                </div>
                <div>
                    <h6>Brands</h6>
                    <ul>
                        <li><a href="#">Toyota</a></li>
                        <li><a href="#">Honda</a></li>
                        <li><a href="#">Nissan</a></li>
                        <li><a href="#">Mazda</a></li>
                        <li><a href="#">Suzuki</a></li>
                    </ul>
                </div>
                <div>
                    <h6>Contact Us</h6>
                    <ul class="footer-contact">
                        <li><i class="bi bi-telephone-fill"></i> +81 3-1234-5678</li>
                        <li><i class="bi bi-envelope-fill"></i> info@tradejapan.co.jp</li>
                        <li><i class="bi bi-geo-alt-fill"></i> 1-1-1 Chuo, Tokyo 100-0001 Japan</li>
                    </ul>
                </div>
            </div>
            <div class="footer-bottom">
                <span>&copy; 2026 Trade Japan. All rights reserved.</span>
                <span class="tagline">MAKE EVERY MILES COUNT</span>
            </div>
        </footer>`;
}

/* ---------- Render Admin Sidebar ---------- */
function renderAdminSidebar(activeItem) {
    const el = document.getElementById('admin-sidebar');
    if (!el) return;
    const role = getRole();
    const isSuperAdmin = role === 'ROLE_SUPER_ADMIN';

    const items = [
        { id: 'dashboard',  icon: 'bi-house-door',       label: 'Dashboard' },
        { id: 'vehicles',   icon: 'bi-truck',            label: 'Vehicles' },
        { id: 'messages',   icon: 'bi-chat-square-text', label: 'Messages' },
        { id: 'customers',  icon: 'bi-people',           label: 'Customers' },
        { id: 'content',    icon: 'bi-file-earmark-text',label: 'Content' },
        { id: 'purchases',  icon: 'bi-bag',              label: 'Purchases' },
        { id: 'testimonials', icon: 'bi-chat-quote',     label: 'Testimonials' },
    ];
    const superItems = [
        { id: 'admins',  icon: 'bi-person-gear',    label: 'Admins' },
        { id: 'reports', icon: 'bi-bar-chart-line',  label: 'Reports' },
    ];

    el.innerHTML = `
        <div class="admin-sidebar">
            <div class="sidebar-logo">
                <div class="logo-icon"><i class="bi bi-car-front-fill"></i></div>
                <div class="logo-name">TRADE JAPAN</div>
                <div class="logo-sub">JAPANESE CARS FOR THE WORLD</div>
            </div>
            <div class="sidebar-nav">
                ${items.map(i => `
                    <a href="#" class="${i.id === activeItem ? 'active' : ''}"
                       onclick="showAdminSection('${i.id}');return false;">
                        <i class="bi ${i.icon}"></i> ${i.label}
                    </a>`).join('')}
                ${isSuperAdmin ? `
                    <div class="nav-section-label">Super Admin</div>
                    ${superItems.map(i => `
                        <a href="#" class="${i.id === activeItem ? 'active' : ''}"
                           onclick="showAdminSection('${i.id}');return false;">
                            <i class="bi ${i.icon}"></i> ${i.label}
                        </a>`).join('')}
                ` : ''}
            </div>
            <div class="sidebar-user">
                <div class="user-avatar"><i class="bi bi-person-fill"></i></div>
                <span class="user-name">${isSuperAdmin ? 'Super Admin' : 'Admin User'}</span>
                <button class="logout-btn" onclick="logout()" title="Logout"><i class="bi bi-box-arrow-right"></i></button>
            </div>
        </div>`;
}

/* ---------- Toggle Password Visibility ---------- */
function togglePassword(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.type = input.type === 'password' ? 'text' : 'password';
    const icon = input.parentElement.querySelector('.input-icon-right i');
    if (icon) icon.className = input.type === 'password' ? 'bi bi-eye' : 'bi bi-eye-slash';
}

/* ---------- Auto-init ---------- */
document.addEventListener('DOMContentLoaded', () => {
    renderNavbar();
    renderFooter();
});


/* ========== NAVBAR SEARCH ========== */
function toggleSearch() {
    const panel = document.getElementById('search-panel');
    if (!panel) return;
    panel.classList.toggle('open');
    if (panel.classList.contains('open')) document.getElementById('search-input').focus();
}

function _searchEsc(t) {
    const d = document.createElement('div');
    d.textContent = t;
    return d.innerHTML;
}

function _searchText(html) {
    const d = document.createElement('div');
    d.innerHTML = html || '';
    return d.textContent || '';
}

function _searchSnippet(text, idx) {
    if (idx < 0) return text.slice(0, 100);
    const start = Math.max(0, idx - 40);
    return (start > 0 ? '...' : '') + text.slice(start, idx + 90) + '...';
}

async function runSearch() {
    const q   = document.getElementById('search-input').value.trim().toLowerCase();
    const out = document.getElementById('search-results');
    if (q.length < 2) { out.innerHTML = '<div class="search-empty">Type at least 2 characters.</div>'; return; }
    out.innerHTML = '<div class="search-empty">Searching...</div>';

    const pages = [
        { key: 'about',    label: 'About Us' },
        { key: 'services', label: 'Services' },
        { key: 'tc',       label: 'Terms & Conditions' },
        { key: 'privacy',  label: 'Privacy Policy' }
    ];

    const [faqRes, ...pageRes] = await Promise.all([
        apiFetch('/faqs'),
        ...pages.map(p => apiFetch('/content/' + p.key))
    ]);

    const hits = [];
    pages.forEach((p, i) => {
        const r = pageRes[i];
        const text = (r.ok && r.data && r.data.htmlContent) ? _searchText(r.data.htmlContent) : '';
        const idx = text.toLowerCase().indexOf(q);
        if (idx >= 0 || p.label.toLowerCase().includes(q)) {
            hits.push({ title: p.label, url: '/pages/content.html?page=' + p.key, text: _searchSnippet(text, idx) });
        }
    });
    if (faqRes.ok && Array.isArray(faqRes.data)) {
        faqRes.data.forEach(f => {
            const ans = _searchText(f.answer);
            const inQ = (f.question || '').toLowerCase().includes(q);
            const idx = ans.toLowerCase().indexOf(q);
            if (inQ || idx >= 0) {
                hits.push({ title: 'FAQ: ' + f.question, url: '/pages/faq.html', text: _searchSnippet(ans, idx) });
            }
        });
    }

    out.innerHTML = hits.length
        ? hits.map(h => `<a class="search-hit" href="${h.url}">
              <div class="search-hit-title">${_searchEsc(h.title)}</div>
              <div class="search-hit-text">${_searchEsc(h.text)}</div></a>`).join('')
        : '<div class="search-empty">No results found.</div>';
}