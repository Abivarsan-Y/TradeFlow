/* ================================================
   TRADE JAPAN — Global JavaScript
   ================================================ */

const API_URL = '/api';

/* ---------- Auth Helpers ---------- */
function getToken()       { return localStorage.getItem('jwt_token'); }
function setToken(token)  { localStorage.setItem('jwt_token', token); }
function getRole()        { return localStorage.getItem('user_role'); }
function setRole(role)    { localStorage.setItem('user_role', role); }
function getUserName()    { return localStorage.getItem('user_name') || ''; }

// Admin and Super Admin accounts are not customers: they cannot use the customer-only features
// (inquiries, wishlist, testimonials). The server refuses them too; this keeps the buttons from failing.
function isStaff() { const r = getRole(); return r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN'; }
function staffBlocked(action) {
    if (!isStaff()) return false;
    const suffix = action ? ` to ${action}` : '';
    const msg = `Logged in as Staff (preview mode). Please log in with a customer account${suffix}.`;
    if (typeof tjToast === 'function') tjToast(msg, true);
    else if (typeof showToast === 'function') showToast(msg, true);
    else alert(msg);
    return true;
}
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
        const text = await response.text();
        let parsed = text;
        try { parsed = JSON.parse(text); } catch { /* plain text */ }
        if (typeof showToast === 'function') {
            showToast((parsed && parsed.message) || "You don't have permission to perform this action.", true);
        }
        return { ok: false, status: 403, data: parsed || null };
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
                ${isStaff() ? '' : '<a href="/pages/profile.html?tab=wishlist" title="Saved Vehicles"><i class="bi bi-cart3"></i></a>'}
                ${rightIcons}
            </div>
        </nav>
        <div class="search-panel" id="search-panel">
            <div class="search-box">
                <i class="bi bi-search"></i>
                <input type="text" id="search-input" placeholder="Search vehicles, brands, auction, inquiry, FAQ..."
                       oninput="searchTyping()"
                       onkeydown="if(event.key==='Enter'){clearTimeout(_searchTimer);runSearch(true);}if(event.key==='Escape')toggleSearch();">
                <button onclick="runSearch(true)">Search</button>
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
                <a class="menu-link" href="/pages/inventory.html">Vehicles</a>
                <a class="menu-link" href="/pages/content.html?page=about">About Us</a>
                <a class="menu-link" href="/pages/content.html?page=services">Services</a>
                <a class="menu-link" href="/pages/faq.html">FAQ</a>
                <a class="menu-link" href="/pages/contact.html">Contact Us</a>
            </div>
            <div class="menu-links-bottom">
                ${token
        ? `<a class="menu-link" href="/pages/profile.html">My Account</a>
                       <a class="menu-link" href="#" onclick="logout()" style="color:var(--tj-maroon)">Logout</a>`
        : `<a class="menu-link" href="/pages/login.html">Login</a>
           <a class="menu-link" href="/pages/register.html">Register</a>
           <a class="menu-link" href="/pages/admin/login.html" style="color:var(--tj-navy);"><i class="bi bi-shield-lock"></i> Admin Login</a>`
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
                        <li><a href="/pages/contact.html">Contact Us</a></li>
                    </ul>
                </div>
                <div>
                    <h6>Information</h6>
                    <ul>
                        <li><a href="/pages/faq.html">FAQ</a></li>
                        <li><a href="/pages/inventory.html">Browse Vehicles</a></li>
                        <li><a href="/pages/content.html?page=services">How To Buy</a></li>
                        <li><a href="/pages/content.html?page=services">Auction Service</a></li>
                        <li><a href="/pages/content.html?page=services">Services</a></li>
                    </ul>
                </div>
                <div>
                    <h6>Brands</h6>
                    <ul>
                        <li><a href="/pages/inventory.html?make=Toyota">Toyota</a></li>
                        <li><a href="/pages/inventory.html?make=Honda">Honda</a></li>
                        <li><a href="/pages/inventory.html?make=Nissan">Nissan</a></li>
                        <li><a href="/pages/inventory.html?make=Mazda">Mazda</a></li>
                        <li><a href="/pages/inventory.html?make=Suzuki">Suzuki</a></li>
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
function toggleAdminSidebar(forceOpen) {
    const sidebar = document.querySelector('.admin-sidebar');
    const overlay = document.querySelector('.admin-sidebar-overlay');
    if (!sidebar) return;
    const open = typeof forceOpen === 'boolean' ? forceOpen : !sidebar.classList.contains('open');
    sidebar.classList.toggle('open', open);
    if (overlay) overlay.classList.toggle('open', open);
}

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
        <div class="admin-mobile-bar">
            <button type="button" class="admin-menu-btn" onclick="toggleAdminSidebar()" aria-label="Open menu">
                <i class="bi bi-list"></i>
            </button>
            <span class="admin-mobile-title"><span style="color:var(--tj-maroon)">TRADE</span> JAPAN</span>
            <a href="/index.html" target="_blank" rel="noopener" class="admin-mobile-home" title="View Website"><i class="bi bi-globe2"></i></a>
        </div>
        <div class="admin-sidebar-overlay" onclick="toggleAdminSidebar(false)"></div>
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
            <div class="sidebar-home">
                <a href="/index.html" target="_blank" rel="noopener"><i class="bi bi-globe2"></i> Home</a>
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

/* ===SEARCH-VEHICLES-START=== */
/* Navbar search: unified word-prefix search across Brands/Categories, Site Features/Pages, FAQs, and Vehicles */
function _searchEsc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}

function _searchStripHtml(html) {
    if (!html) return '';
    const d = document.createElement('div');
    d.innerHTML = html;
    return d.textContent || d.innerText || '';
}

function _searchWords(text) {
    return String(text || '').toLowerCase().split(/[^a-z0-9]+/).filter(Boolean);
}

/** Returns 0 if any query word has no word-start prefix match; otherwise higher score for exact whole-word hits. */
function _searchPrefixScore(text, queryWords) {
    const targetWords = _searchWords(text);
    if (!targetWords.length || !queryWords.length) return 0;
    let total = 0;
    for (const qw of queryWords) {
        let best = 0;
        for (const tw of targetWords) {
            if (tw === qw) { best = 2; break; }
            if (tw.startsWith(qw) && best < 1) best = 1;
        }
        if (best === 0) return 0;
        total += best;
    }
    return total;
}

const _SEARCH_PAGES = [
    {
        title: 'Auction Service & How To Buy',
        desc: 'Bid at Japanese car auctions, security deposit, and buying steps',
        url: '/pages/content.html?page=services',
        tag: 'Feature',
        keywords: 'auction auctions bidding bid deposit services how to buy import inspection'
    },
    {
        title: 'Browse Vehicles & Inventory',
        desc: 'Filter available cars by make, model, year, body type, and price',
        url: '/pages/inventory.html',
        tag: 'Page',
        keywords: 'vehicles inventory catalogue stock cars browse filter buy'
    },
    {
        title: 'Send Vehicle Inquiry',
        desc: 'Choose a vehicle and click Inquire Now to ask about price or shipping',
        url: '/pages/inventory.html',
        tag: 'Feature',
        keywords: 'inquire inquiry quote price shipping question vehicle'
    },
    {
        title: 'My Inquiries & Messages',
        desc: 'View replies from the Trade Japan team on your vehicle inquiries',
        url: '/pages/profile.html?tab=messages',
        tag: 'Account',
        keywords: 'inquire inquiry messages replies chat account',
        staffUrl: '/pages/admin/dashboard.html',
        staffDesc: 'Manage customer inquiries and replies in Admin Panel'
    },
    {
        title: 'Saved Vehicles (Wishlist)',
        desc: 'View vehicles you saved to your wishlist',
        url: '/pages/profile.html?tab=wishlist',
        tag: 'Account',
        keywords: 'wishlist saved favorites heart cart',
        hideForStaff: true
    },
    {
        title: 'Purchase History & Invoices',
        desc: 'Track order progress, advance payments, balance, and PDF invoices',
        url: '/pages/profile.html?tab=history',
        tag: 'Account',
        keywords: 'purchase history orders invoice pdf payment balance delivery',
        staffUrl: '/pages/admin/dashboard.html',
        staffDesc: 'Manage customer purchases and invoices in Admin Panel'
    },
    {
        title: 'Contact Us',
        desc: 'Send a direct message, view phone, email, and Tokyo office details',
        url: '/pages/contact.html',
        tag: 'Page',
        keywords: 'contact us message support email phone address help'
    },
    {
        title: 'Frequently Asked Questions (FAQ)',
        desc: 'Answers on auctions, payments, shipping times, and inspections',
        url: '/pages/faq.html',
        tag: 'Page',
        keywords: 'faq questions answers help payment shipping cancel inspection'
    },
    {
        title: 'Share Your Experience (Testimonials)',
        desc: 'Read or submit a customer testimonial and rating',
        url: '/pages/submit-testimonial.html',
        tag: 'Feature',
        keywords: 'testimonial testimonials review feedback rating experience'
    },
    {
        title: 'About Us',
        desc: 'About Trade Japan Co., Ltd., mission, vision, and company profile',
        url: '/pages/content.html?page=about',
        tag: 'Company',
        keywords: 'about us company profile mission vision established japan chiba bank'
    },
    {
        title: 'Terms & Conditions',
        desc: 'Pricing terms (FOB, C&F, CIF), auction rules, and cancellation terms',
        url: '/pages/content.html?page=tc',
        tag: 'Policy',
        keywords: 'terms conditions fob c&f cif cancellation agreement policy'
    },
    {
        title: 'Privacy Policy',
        desc: 'How Trade Japan collects, uses, and protects your account information',
        url: '/pages/content.html?page=privacy',
        tag: 'Policy',
        keywords: 'privacy policy personal data security account'
    }
];

let _searchCache = null;
let _searchTimer = null;

function searchTyping() {
    clearTimeout(_searchTimer);
    _searchTimer = setTimeout(() => runSearch(false), 250);
}

async function _searchEnsureData() {
    if (_searchCache) return _searchCache;
    const [vehRes, faqRes] = await Promise.all([
        apiFetch('/vehicles?page=0&size=500'),
        apiFetch('/faqs')
    ]);
    if (!vehRes.ok || !Array.isArray(vehRes.data)) return null;
    _searchCache = {
        vehicles: vehRes.data,
        faqs: (faqRes.ok && Array.isArray(faqRes.data)) ? faqRes.data : []
    };
    return _searchCache;
}

function _searchVehicleHay(v) {
    return [v.make, v.model, v.manufactureYear, v.category, v.exteriorColor, v.fuelType, v.transmission,
        v.steering, v.driveType, v.refNo, v.grade, v.engineType,
        (v.condition || '').toUpperCase() === 'NEW' ? 'new' : 'used']
        .filter(x => x !== null && x !== undefined && x !== '')
        .join(' ');
}

const SEARCH_MAX = 8;
const SEARCH_SKIP = ['vehicle', 'vehicles', 'car', 'cars'];

async function runSearch(navigateTop = false) {
    const input = document.getElementById('search-input');
    const out   = document.getElementById('search-results');
    if (!input || !out) return;
    const q = input.value.trim().toLowerCase();
    if (q.length < 2) {
        out.innerHTML = '<div class="search-empty">Type at least 2 characters (e.g. a car name, brand, auction, inquire, or FAQ).</div>';
        return;
    }

    if (!_searchCache) out.innerHTML = '<div class="search-empty">Searching...</div>';
    const data = await _searchEnsureData();
    if (!data) {
        out.innerHTML = '<div class="search-empty">Could not load search results. Please try again.</div>';
        return;
    }
    if (input.value.trim().toLowerCase() !== q) return;

    const rawWords = _searchWords(q);
    if (!rawWords.length) {
        out.innerHTML = '<div class="search-empty">Type a keyword to search.</div>';
        return;
    }
    const queryWords = rawWords.length > 1
        ? rawWords.filter(w => !SEARCH_SKIP.includes(w))
        : rawWords;

    const staff = isStaff();

    // 1. Brand & Category filter shortcuts (derived from live vehicles)
    const makeCounts = new Map();
    const bodyCounts = new Map();
    for (const v of data.vehicles) {
        if (v.make) makeCounts.set(v.make, (makeCounts.get(v.make) || 0) + 1);
        if (v.category) bodyCounts.set(v.category, (bodyCounts.get(v.category) || 0) + 1);
    }
    const filterHits = [];
    for (const [make, count] of makeCounts.entries()) {
        const score = _searchPrefixScore(make, queryWords);
        if (score > 0) {
            filterHits.push({
                score,
                title: `${make} — Browse ${make} Vehicles`,
                desc: `Open inventory filtered by brand ${make} (${count} vehicle${count === 1 ? '' : 's'})`,
                url: `/pages/inventory.html?make=${encodeURIComponent(make)}`,
                tag: 'Brand'
            });
        }
    }
    for (const [body, count] of bodyCounts.entries()) {
        const score = _searchPrefixScore(body, queryWords);
        if (score > 0) {
            filterHits.push({
                score,
                title: `${body} — Browse ${body} Vehicles`,
                desc: `Open inventory filtered by ${body} (${count} vehicle${count === 1 ? '' : 's'})`,
                url: `/pages/inventory.html?bodyType=${encodeURIComponent(body)}`,
                tag: 'Body Type'
            });
        }
    }
    filterHits.sort((a, b) => b.score - a.score || a.title.localeCompare(b.title));

    // 2. Site Pages, Features & FAQs
    const pageHits = [];
    for (const p of _SEARCH_PAGES) {
        if (staff && p.hideForStaff) continue;
        const score = _searchPrefixScore(`${p.title} ${p.keywords}`, queryWords);
        if (score > 0) {
            pageHits.push({
                score,
                title: p.title,
                desc: (staff && p.staffDesc) ? p.staffDesc : p.desc,
                url: (staff && p.staffUrl) ? p.staffUrl : p.url,
                tag: p.tag
            });
        }
    }
    for (const f of data.faqs) {
        const answerText = _searchStripHtml(f.answer);
        const score = _searchPrefixScore(`${f.question} ${answerText}`, queryWords);
        if (score > 0) {
            const snippet = answerText.trim().slice(0, 110) + (answerText.trim().length > 110 ? '…' : '');
            pageHits.push({
                score,
                title: f.question,
                desc: snippet || 'View answer on the FAQ page',
                url: `/pages/faq.html?id=${encodeURIComponent(f.id)}`,
                tag: 'FAQ'
            });
        }
    }
    pageHits.sort((a, b) => b.score - a.score);

    // 3. Matching Vehicles
    const rank = { AVAILABLE: 0, RESERVED: 1, SOLD: 2 };
    const vehicleHits = data.vehicles
        .map(v => {
            const nameScore = _searchPrefixScore(`${v.make || ''} ${v.model || ''} ${v.refNo || ''}`, queryWords);
            const fullScore = nameScore || _searchPrefixScore(_searchVehicleHay(v), queryWords);
            const exactRef = v.refNo && v.refNo.toLowerCase() === q;
            return { v, score: fullScore + (nameScore ? 2 : 0) + (exactRef ? 10 : 0), exactRef };
        })
        .filter(x => x.score > 0)
        .sort((a, b) => b.score - a.score
            || ((rank[(a.v.status || 'AVAILABLE').toUpperCase()] ?? 3) - (rank[(b.v.status || 'AVAILABLE').toUpperCase()] ?? 3)));

    // If user pressed Enter / clicked Search, navigate to the best match
    if (navigateTop) {
        let topUrl = null;
        if (vehicleHits.length && vehicleHits[0].exactRef) {
            topUrl = `/pages/vehicle-detail.html?id=${vehicleHits[0].v.id}`;
        } else if (filterHits.length && filterHits[0].tag === 'Brand') {
            topUrl = filterHits[0].url;
        } else if (vehicleHits.length && (!pageHits.length || vehicleHits[0].score >= pageHits[0].score + 2)) {
            topUrl = `/pages/vehicle-detail.html?id=${vehicleHits[0].v.id}`;
        } else if (pageHits.length) {
            topUrl = pageHits[0].url;
        } else if (filterHits.length) {
            topUrl = filterHits[0].url;
        } else if (vehicleHits.length) {
            topUrl = `/pages/vehicle-detail.html?id=${vehicleHits[0].v.id}`;
        }
        if (topUrl) {
            window.location.href = topUrl;
            return;
        }
    }

    if (!filterHits.length && !pageHits.length && !vehicleHits.length) {
        out.innerHTML = '<div class="search-empty">No results found. Try a vehicle name, brand (e.g. Toyota), or feature (e.g. auction, inquire, FAQ).</div>';
        return;
    }

    let html = '';
    if (filterHits.length) {
        html += `<div class="search-group-title">Brands &amp; Categories</div>`;
        html += filterHits.slice(0, 4).map(h => `
            <a class="search-hit" href="${h.url}">
                <div>
                    <div class="search-hit-title">${_searchEsc(h.title)}</div>
                    <div class="search-hit-text">${_searchEsc(h.desc)}</div>
                </div>
                <span class="search-tag">${_searchEsc(h.tag)}</span>
            </a>`).join('');
    }
    if (pageHits.length) {
        html += `<div class="search-group-title">Pages, Features &amp; FAQs</div>`;
        html += pageHits.slice(0, 5).map(h => `
            <a class="search-hit" href="${h.url}">
                <div>
                    <div class="search-hit-title">${_searchEsc(h.title)}</div>
                    <div class="search-hit-text">${_searchEsc(h.desc)}</div>
                </div>
                <span class="search-tag">${_searchEsc(h.tag)}</span>
            </a>`).join('');
    }
    if (vehicleHits.length) {
        html += `<div class="search-group-title">Vehicles (${vehicleHits.length})</div>`;
        html += vehicleHits.slice(0, SEARCH_MAX).map(({ v }) => {
            const img    = (Array.isArray(v.images) && v.images[0]?.imageUrl)
                ? v.images[0].imageUrl.trim()
                : (v.imageUrls ? v.imageUrls.split(',')[0].trim() : '');
            const status = (v.status || 'AVAILABLE').toUpperCase();
            const meta   = [v.manufactureYear, v.category,
                (v.mileage || v.mileage === 0) ? Number(v.mileage).toLocaleString('en-US') + ' km' : '',
                (v.condition || '').toUpperCase() === 'NEW' ? 'New' : 'Used'].filter(Boolean).join('  \u00b7  ');
            return `<a class="search-vcard" href="/pages/vehicle-detail.html?id=${v.id}">
                  <div class="search-vimg">${img ? `<img src="${_searchEsc(img)}" alt="" loading="lazy">` : '<i class="bi bi-car-front"></i>'}</div>
                  <div class="search-vinfo">
                      <div class="search-hit-title">${_searchEsc(v.make)} ${_searchEsc(v.model)}</div>
                      <div class="search-hit-text">${_searchEsc(meta)}</div>
                      <div class="search-vprice">$${Number(v.fobPrice || 0).toLocaleString('en-US')}
                          <span class="search-vstatus ${status.toLowerCase()}">${status.charAt(0) + status.slice(1).toLowerCase()}</span></div>
                  </div></a>`;
        }).join('');
        if (vehicleHits.length > SEARCH_MAX) {
            html += `<div class="search-note">Showing top ${SEARCH_MAX} of ${vehicleHits.length} matching vehicles.</div>`;
        }
    }
    out.innerHTML = html;
}
/* ===SEARCH-VEHICLES-END=== */

/* ---------- Date helpers: always YYYY-MM-DD (and YYYY-MM-DD HH:mm) ---------- */
function tjDate(d) {
    if (!d) return '';
    const t = new Date(d);
    if (isNaN(t)) return '';
    const p = n => String(n).padStart(2, '0');
    return `${t.getFullYear()}-${p(t.getMonth() + 1)}-${p(t.getDate())}`;
}

function tjDateTime(d) {
    if (!d) return '';
    const t = new Date(d);
    if (isNaN(t)) return '';
    const p = n => String(n).padStart(2, '0');
    return `${tjDate(t)} ${p(t.getHours())}:${p(t.getMinutes())}`;
}

/* ---------- Global escapeHtml helper ---------- */
function escapeHtml(text) {
    if (text == null) return '';
    const d = document.createElement('div');
    d.textContent = String(text);
    return d.innerHTML;
}