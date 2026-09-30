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
function staffBlocked() {
    if (!isStaff()) return false;
    const msg = 'Customer accounts only. Please log in with a customer account to use this.';
    if (typeof tjToast === 'function') tjToast(msg, true); else alert(msg);
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
                ${isStaff() ? '' : '<a href="/pages/profile.html?tab=wishlist" title="Saved Vehicles"><i class="bi bi-cart3"></i></a>'}
                ${rightIcons}
            </div>
        </nav>
        <div class="search-panel" id="search-panel">
            <div class="search-box">
                <i class="bi bi-search"></i>
                <input type="text" id="search-input" placeholder="Search vehicles: make, model, year, reference no..."
                       oninput="searchTyping()"
                       onkeydown="if(event.key==='Enter'){clearTimeout(_searchTimer);runSearch();}if(event.key==='Escape')toggleSearch();">
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
/* Navbar search: finds VEHICLES (make, model, year, body type, colour, fuel, transmission, reference no ...) */
function _searchEsc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}

let _searchVehicles = null;   // the vehicle list, loaded once and reused
let _searchTimer = null;

// live search while typing (small delay so it does not search on every key)
function searchTyping() {
    clearTimeout(_searchTimer);
    _searchTimer = setTimeout(runSearch, 300);
}

// everything about a vehicle that a visitor may type, as one lower-case text
function _searchHay(v) {
    return [v.make, v.model, v.manufactureYear, v.category, v.exteriorColor, v.fuelType, v.transmission,
            v.steering, v.driveType, v.refNo, v.grade, v.engineType,
            (v.condition || '').toUpperCase() === 'NEW' ? 'new' : 'used']
        .filter(x => x !== null && x !== undefined && x !== '')
        .join(' ').toLowerCase();
}

// 1-2 letters (like "to") must start a word, longer words may be anywhere (so "au" does not match "automatic")
function _searchHas(hay, w) {
    return w.length > 2 ? hay.includes(w) : (' ' + hay).includes(' ' + w);
}

const SEARCH_MAX = 8;                                          // cards shown at most
const SEARCH_SKIP = ['vehicle', 'vehicles', 'car', 'cars'];    // words that say nothing about which car

async function runSearch() {
    const input = document.getElementById('search-input');
    const out   = document.getElementById('search-results');
    if (!input || !out) return;
    const q = input.value.trim().toLowerCase();
    if (q.length < 2) {
        out.innerHTML = '<div class="search-empty">Type a make, model, year or reference number (at least 2 characters).</div>';
        return;
    }
    if (_searchVehicles === null) {
        out.innerHTML = '<div class="search-empty">Searching...</div>';
        const res = await apiFetch('/vehicles?page=0&size=500');
        if (!res.ok || !Array.isArray(res.data)) {
            out.innerHTML = '<div class="search-empty">Could not load vehicles. Please try again.</div>';
            return;
        }
        _searchVehicles = res.data;
    }
    if (input.value.trim().toLowerCase() !== q) return;   // the visitor kept typing: a newer search is running

    // every word typed must match: "toyota corolla 2018" finds only vehicles with all three
    const words = q.split(/\s+/).filter(w => w && !SEARCH_SKIP.includes(w));
    const rank  = { AVAILABLE: 0, RESERVED: 1, SOLD: 2 };
    const hits  = _searchVehicles
        .filter(v => { const hay = _searchHay(v); return words.every(w => _searchHas(hay, w)); })
        .sort((a, b) => (rank[(a.status || 'AVAILABLE').toUpperCase()] ?? 3) - (rank[(b.status || 'AVAILABLE').toUpperCase()] ?? 3));

    if (!hits.length) {
        out.innerHTML = '<div class="search-empty">No vehicles found. Try a make, a model or a year (for example: toyota corolla 2018).</div>';
        return;
    }
    const cards = hits.slice(0, SEARCH_MAX).map(v => {
        const img    = v.imageUrls ? v.imageUrls.split(',')[0].trim() : '';
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
    const note = hits.length > SEARCH_MAX
        ? `<div class="search-note">Showing ${SEARCH_MAX} of ${hits.length} vehicles. Add more words to narrow down (for example: toyota corolla 2018).</div>`
        : `<div class="search-note">${hits.length} vehicle${hits.length === 1 ? '' : 's'} found.</div>`;
    out.innerHTML = note + cards;
}
/* ===SEARCH-VEHICLES-END=== */
