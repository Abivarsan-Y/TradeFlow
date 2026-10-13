/* ================================================
   TRADE JAPAN — Vehicle (Inventory) public scripts
   Uses apiFetch / renderNavbar from app.js
   ================================================ */

const FALLBACK_IMG = null; // no image => car icon placeholder (matches existing design)

function vEsc(t) {
    const d = document.createElement('div');
    d.textContent = t == null ? '' : String(t);
    return d.innerHTML;
}
function vMoney(n) { return '$' + Number(n || 0).toLocaleString('en-US'); }
function vImages(v) {
    return v.imageUrls ? v.imageUrls.split(',').map(s => s.trim()).filter(Boolean) : [];
}
function vStatusBadge(status) {
    const s = (status || 'AVAILABLE').toUpperCase();
    const cls = s === 'AVAILABLE' ? 'v-status-available' : (s === 'RESERVED' ? 'v-status-reserved' : 'v-status-sold');
    return `<span class="v-status ${cls}">${s.charAt(0) + s.slice(1).toLowerCase()}</span>`;
}

/* ---------- Vehicle card (uses existing .vehicle-card design) ---------- */
// "NEW ARRIVAL" tag = the admin marked this vehicle as a new arrival
function vIsNewArrival(v) {
    return v.isNewArrival === true;
}

function vehicleCardHtml(v, showNewTag) {
    const img = vImages(v)[0];
    return `
        <a class="vehicle-card vehicle-link" href="/pages/vehicle-detail.html?id=${v.id}">
            <div class="vehicle-img">
                ${img ? `<img src="${vEsc(img)}" alt="${vEsc(v.make)} ${vEsc(v.model)}" loading="lazy">`
                      : '<i class="bi bi-car-front"></i>'}
                ${vStatusBadge(v.status)}
                ${showNewTag === true && vIsNewArrival(v) ? '<span class="v-new-tag">NEW ARRIVAL</span>' : ''}
            </div>
            <div class="vehicle-info">
                <div class="vehicle-name">${vEsc(v.make)} ${vEsc(v.model)}</div>
                <div class="vehicle-meta">${vEsc(v.manufactureYear)} &nbsp;|&nbsp; ${vMoney(v.fobPrice)}</div>
            </div>
        </a>`;
}

/* ---------- Home page: New Arrivals ---------- */
async function loadNewArrivals(containerId) {
    const el = document.getElementById(containerId);
    if (!el) return;
    const res = await apiFetch('/vehicles/new-arrivals');
    if (res.ok && Array.isArray(res.data) && res.data.length) {
        el.innerHTML = res.data.slice(0, 4).map(vehicleCardHtml).join('');
    } else {
        el.innerHTML = '<div class="vehicle-empty">No vehicles available yet.</div>';
    }
}

/* ---------- Inventory (catalogue) page: sidebar filters + sort + infinite scroll ---------- */
const INV = { all: [], filtered: [], shown: 0, step: 12, busy: false };

const INV_SORTS = {
    newest:    (a, b) => (new Date(b.createdAt || 0) - new Date(a.createdAt || 0)) || (b.id - a.id),
    oldest:    (a, b) => (new Date(a.createdAt || 0) - new Date(b.createdAt || 0)) || (a.id - b.id),
    priceLow:  (a, b) => Number(a.fobPrice) - Number(b.fobPrice),
    priceHigh: (a, b) => Number(b.fobPrice) - Number(a.fobPrice),
    yearNew:   (a, b) => Number(b.manufactureYear) - Number(a.manufactureYear),
    yearOld:   (a, b) => Number(a.manufactureYear) - Number(b.manufactureYear)
};

const invVal = id => (document.getElementById(id)?.value || '').trim();

/* fill a <select> with the distinct values found in the data (plus a value that came from the URL) */
function invFillSelect(id, values, firstLabel, wanted, descending) {
    const el = document.getElementById(id);
    const list = [...new Set(values.filter(v => v !== null && v !== undefined && v !== '').map(String))];
    if (wanted && !list.includes(wanted)) list.push(wanted);
    list.sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
    if (descending) list.reverse();
    el.innerHTML = `<option value="">${firstLabel}</option>` + list.map(v => `<option value="${vEsc(v)}">${vEsc(v)}</option>`).join('');
    el.value = wanted || '';
}

/* the Model list only shows models of the chosen Make */
function invRefreshModels(wantedModel) {
    const make = invVal('f-make');
    invFillSelect('f-model', INV.all.filter(v => !make || v.make === make).map(v => v.model), 'All Models', wantedModel);
}

/* price slider <-> Max box */
function invSyncSlider() {
    const slider = document.getElementById('f-slider');
    const max = invVal('f-max');
    slider.value = max === '' ? slider.max : Math.min(Number(max), Number(slider.max));
}
function invSliderMoved() {
    const slider = document.getElementById('f-slider');
    document.getElementById('f-max').value = Number(slider.value) >= Number(slider.max) ? '' : slider.value;
}

function invApply() {
    const make = invVal('f-make'), model = invVal('f-model'), year = invVal('f-year'), body = invVal('f-body'),
          steering = invVal('f-steering'), status = invVal('f-status'),
          min = invVal('f-min'), max = invVal('f-max'), sort = invVal('f-sort') || 'newest';

    INV.filtered = INV.all.filter(v =>
        (!make || v.make === make) &&
        (!model || v.model === model) &&
        (!year || String(v.manufactureYear) === year) &&
        (!body || v.category === body) &&
        (!steering || v.steering === steering) &&
        (!status || (v.status || '').toUpperCase() === status) &&
        (min === '' || Number(v.fobPrice) >= Number(min)) &&
        (max === '' || Number(v.fobPrice) <= Number(max))
    ).sort(INV_SORTS[sort] || INV_SORTS.newest);

    INV.shown = 0;
    document.getElementById('inv-grid').innerHTML = '';
    invRenderMore();
}

function invRenderMore() {
    const next = INV.filtered.slice(INV.shown, INV.shown + INV.step);
    document.getElementById('inv-grid').insertAdjacentHTML('beforeend', next.map(v => vehicleCardHtml(v, true)).join(''));
    INV.shown += next.length;

    const total = INV.filtered.length;
    document.getElementById('inv-count').textContent =
        `Vehicles Found (Showing ${INV.shown}${INV.shown < total ? ' of ' + total : ''})`;
    document.getElementById('inv-empty').style.display = total === 0 ? 'block' : 'none';
    document.getElementById('inv-loading').style.display = INV.shown < total ? 'flex' : 'none';
    invFillViewport();
}

/* infinite scroll: when the "Loading more vehicles..." line comes into view, add the next batch */
function invFillViewport() {
    if (INV.busy || INV.shown >= INV.filtered.length) return;
    const marker = document.getElementById('inv-loading');
    if (marker.getBoundingClientRect().top < window.innerHeight + 200) {
        INV.busy = true;
        setTimeout(() => { INV.busy = false; invRenderMore(); }, 250);
    }
}

function invClear() {
    ['f-make', 'f-year', 'f-body', 'f-steering', 'f-status', 'f-min', 'f-max'].forEach(id => document.getElementById(id).value = '');
    invRefreshModels('');
    invSyncSlider();
    invApply();
}

async function initInventory() {
    const res = await apiFetch('/vehicles?page=0&size=500');
    if (!res.ok || !Array.isArray(res.data)) {
        document.getElementById('inv-grid').innerHTML = '<div class="vehicle-empty">Could not load vehicles. Please try again.</div>';
        return;
    }
    INV.all = res.data;
    const qp = new URLSearchParams(window.location.search);

    // option lists come from the real vehicles
    invFillSelect('f-make', INV.all.map(v => v.make), 'All Makes', qp.get('make') || '');
    invRefreshModels(qp.get('model') || '');
    invFillSelect('f-year', INV.all.map(v => v.manufactureYear), 'All Years', qp.get('year') || '', true);
    invFillSelect('f-body', INV.all.map(v => v.category), 'All Body Types', qp.get('bodyType') || '');
    [['steering', 'f-steering'], ['status', 'f-status'], ['sort', 'f-sort'], ['minPrice', 'f-min'], ['maxPrice', 'f-max']]
        .forEach(([k, id]) => { if (qp.get(k)) document.getElementById(id).value = qp.get(k); });

    // price slider goes up to the most expensive vehicle
    const top = Math.max(0, ...INV.all.map(v => Number(v.fobPrice) || 0));
    document.getElementById('f-slider').max = Math.max(1000, Math.ceil(top / 1000) * 1000);
    invSyncSlider();

    document.getElementById('f-make').onchange = () => invRefreshModels('');
    document.getElementById('f-slider').oninput = invSliderMoved;
    document.getElementById('f-max').oninput = invSyncSlider;
    document.getElementById('inv-search').onclick = invApply;
    document.getElementById('inv-clear').onclick = invClear;
    document.getElementById('f-sort').onchange = invApply;
    ['f-min', 'f-max'].forEach(id => document.getElementById(id).addEventListener('keydown', e => { if (e.key === 'Enter') invApply(); }));
    window.addEventListener('scroll', invFillViewport, { passive: true });

    invApply();
}

/* ---------- Vehicle detail page ---------- */

// WhatsApp number used by the "WhatsApp" button (country code + number, digits only).
// It is set in application.properties (app.whatsapp-number) and loaded from /api/settings/public.
// While it is empty the WhatsApp button is simply hidden.
let TJ_WHATSAPP_NUMBER = '';

/* the 18 specifications, in display order */
const VD_SPECS = [
    ['Ref No',        v => v.refNo ? '#' + v.refNo : null],
    ['Body Type',     v => v.category],
    ['Chassis No',    v => v.vin],
    ['Model Code',    v => v.modelCode],
    ['Grade',         v => v.grade],
    ['Engine Model',  v => v.engineType],
    ['Odometer',      v => v.mileage != null ? Number(v.mileage).toLocaleString('en-US') + ' km' : null],
    ['Displacement',  v => v.displacementCc != null ? Number(v.displacementCc).toLocaleString('en-US') + ' cc' : null],
    ['Steering',      v => v.steering],
    ['Transmission',  v => v.transmission],
    ['Doors',         v => v.doors],
    ['Passengers',    v => v.passengers],
    ['Dimension',     v => v.dimension],
    ['Size',          v => v.size],
    ['Exterior Color',v => v.exteriorColor],
    ['Drive Type',    v => v.driveType],
    ['Fuel Type',     v => v.fuelType],
    ['Location',      v => v.inventoryLocation]
];

const VD = { imgs: [], idx: 0 };

async function initVehicleDetail() {
    const box = document.getElementById('vd-box');
    const id = new URLSearchParams(window.location.search).get('id');
    if (!id) { box.innerHTML = '<div class="vehicle-empty">No vehicle selected.</div>'; return; }

    const [res, featRes, settingsRes] = await Promise.all([
        apiFetch('/vehicles/' + encodeURIComponent(id)), apiFetch('/features'), apiFetch('/settings/public')]);
    TJ_WHATSAPP_NUMBER = (settingsRes.ok && settingsRes.data && settingsRes.data.whatsappNumber)
        ? String(settingsRes.data.whatsappNumber).replace(/\D/g, '') : '';
    if (!res.ok || !res.data || typeof res.data !== 'object') {
        box.innerHTML = '<div class="vehicle-empty">Vehicle not found.</div>';
        document.getElementById('vd-crumb').textContent = 'Not found';
        return;
    }
    const v = res.data;
    const catalog = (featRes.ok && Array.isArray(featRes.data)) ? featRes.data : [];
    const onNames = new Set((v.features || []).map(f => f.name));

    VD.imgs = vImages(v);
    VD.idx = 0;

    const title = `${v.make} ${v.model} ${v.manufactureYear}`;
    document.getElementById('vd-crumb').textContent = `${v.make} ${v.model} ${v.manufactureYear}`;
    document.title = `${title} - Trade Japan`;

    const isSold = (v.status || '').toUpperCase() === 'SOLD';
    const waText = `Hello Trade Japan, I am interested in the ${title} (Ref ${v.refNo || ('TJ' + v.id)}).`;
    const waUrl = `https://wa.me/${TJ_WHATSAPP_NUMBER}?text=${encodeURIComponent(waText)}`;
    const nav = VD.imgs.length > 1;

    const fact = (icon, label, value) => `
        <div class="vd-fact">
            <i class="bi ${icon}"></i>
            <div><span>${label}</span><strong>${value == null || value === '' ? '—' : vEsc(value)}</strong></div>
        </div>`;

    const specCells = VD_SPECS.map(([label, get]) => {
        const val = get(v);
        return `<div class="vd-spec-cell"><span>${label}</span><strong>${val == null || val === '' ? '–' : vEsc(val)}</strong></div>`;
    }).join('');

    const featureCells = catalog.map(f => {
        const on = onNames.has(f.name);
        return `<div class="vd-opt ${on ? 'on' : 'off'}">${on ? '<i class="bi bi-check-lg"></i>' : ''}<span>${vEsc(f.name)}</span></div>`;
    }).join('');

    box.innerHTML = `
        <a class="vd-back" href="/pages/inventory.html"><i class="bi bi-arrow-left"></i> Back to catalogue</a>

        <div class="vd-top">
            <div class="vd-left">
                <div class="vd-main">
                    ${VD.imgs.length ? `<img id="vd-main-img" src="${vEsc(VD.imgs[0])}" alt="${vEsc(title)}">` : '<i class="bi bi-car-front vd-noimg"></i>'}
                    <span class="vd-badge">${String(v.condition || '').toUpperCase() === 'NEW' ? 'New Vehicle' : 'Used Vehicle'}</span>
                    ${VD.imgs.length ? `<span class="vd-counter" id="vd-counter">1 / ${VD.imgs.length}</span>` : ''}
                    ${nav ? `<button class="vd-arrow prev" onclick="vdStep(-1)" aria-label="Previous photo"><i class="bi bi-chevron-left"></i></button>
                             <button class="vd-arrow next" onclick="vdStep(1)" aria-label="Next photo"><i class="bi bi-chevron-right"></i></button>` : ''}
                </div>
                ${nav ? `<div class="vd-thumbs">${VD.imgs.map((u, i) =>
                    `<button class="vd-thumb${i === 0 ? ' active' : ''}" onclick="vdSelectImage(${i})"><img src="${vEsc(u)}" alt=""></button>`).join('')}</div>` : ''}
            </div>

            <div class="vd-side">
                <div class="vd-head">${vStatusBadge(v.status)}<span class="vd-ref">Ref: ${vEsc(v.refNo || ('TJ' + v.id))}</span></div>
                <h1 class="vd-title">${vEsc(title)}</h1>
                <div class="vd-price">${vMoney(v.fobPrice)} <small>FOB</small></div>

                <div class="vd-facts">
                    ${fact('bi-speedometer2', 'Mileage', v.mileage != null ? Number(v.mileage).toLocaleString('en-US') + ' km' : null)}
                    ${fact('bi-calendar3', 'Year', v.manufactureYear)}
                    ${fact('bi-gear', 'Engine', v.engineType)}
                    ${fact('bi-geo-alt', 'Location', v.inventoryLocation)}
                </div>

                ${isSold
                    ? '<button class="vd-btn vd-btn-inquire" disabled><i class="bi bi-slash-circle"></i> Sold</button>'
                    : `<button class="vd-btn vd-btn-inquire" data-vid="${v.id}" data-vname="${vEsc(title)}" onclick="openInquiryModal(this)"><i class="bi bi-envelope"></i> Inquire Now</button>`}
                ${TJ_WHATSAPP_NUMBER ? `<a class="vd-btn vd-btn-wa" href="${waUrl}" target="_blank" rel="noopener"><i class="bi bi-whatsapp"></i> WhatsApp</a>` : ''}
                <button class="vd-btn vd-btn-save" id="vd-save-btn" data-vid="${v.id}" data-saved="0" onclick="vdToggleSave(this)"><i class="bi bi-heart"></i> Save Vehicle</button>
            </div>
        </div>

        <div class="vd-tabs-card">
            <div class="vd-tabs">
                <button class="vd-tab active" onclick="vdShowTab('overview', this)">Overview</button>
                <button class="vd-tab" onclick="vdShowTab('specs', this)">Specifications</button>
                <button class="vd-tab" onclick="vdShowTab('features', this)">Features</button>
            </div>

            <div class="vd-panel" id="vd-tab-overview">
                ${v.description && v.description.trim()
                    ? `<p class="vd-overview">${vEsc(v.description)}</p>`
                    : '<p class="vd-muted">No overview has been added for this vehicle yet.</p>'}
            </div>

            <div class="vd-panel" id="vd-tab-specs" style="display:none;">
                <h3 class="vd-panel-title">Specifications</h3>
                <div class="vd-spec-grid">${specCells}</div>
            </div>

            <div class="vd-panel" id="vd-tab-features" style="display:none;">
                <h3 class="vd-panel-title">Features</h3>
                ${catalog.length ? `<div class="vd-opt-grid">${featureCells}</div>` : '<p class="vd-muted">No feature list available.</p>'}
            </div>
        </div>`;

    vdInitSaveButton(Number(v.id));
}

/* ---------- Wishlist ("Save Vehicle") ---------- */
function vdSetSaved(btn, saved) {
    btn.dataset.saved = saved ? '1' : '0';
    btn.classList.toggle('saved', saved);
    btn.innerHTML = saved ? '<i class="bi bi-heart-fill"></i> Saved' : '<i class="bi bi-heart"></i> Save Vehicle';
}

// when a customer is logged in, fill the heart if this vehicle is already in their wishlist
async function vdInitSaveButton(vehicleId) {
    const btn = document.getElementById('vd-save-btn');
    if (!btn || !getToken()) return;
    const res = await apiFetch('/wishlist/ids');
    if (res.ok && Array.isArray(res.data) && res.data.includes(vehicleId)) vdSetSaved(btn, true);
}

async function vdToggleSave(btn) {
    if (!getToken()) {   // not logged in: go to the login page and come back here afterwards
        window.location.href = '/pages/login.html?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
        return;
    }
    const wasSaved = btn.dataset.saved === '1';
    btn.disabled = true;
    const res = await apiFetch('/wishlist/' + btn.dataset.vid, { method: wasSaved ? 'DELETE' : 'POST' });
    btn.disabled = false;
    if (res.ok) {
        vdSetSaved(btn, !wasSaved);
        if (typeof tjToast === 'function') tjToast(wasSaved ? 'Removed from your wishlist.' : 'Saved to your wishlist.');
    } else if (res.status !== 401 && typeof tjToast === 'function') {
        tjToast('Could not update your wishlist. Please try again.', true);
    }
}

function vdRenderImage() {
    const img = document.getElementById('vd-main-img');
    if (img) img.src = VD.imgs[VD.idx];
    const counter = document.getElementById('vd-counter');
    if (counter) counter.textContent = `${VD.idx + 1} / ${VD.imgs.length}`;
    document.querySelectorAll('.vd-thumb').forEach((t, i) => t.classList.toggle('active', i === VD.idx));
}
function vdSelectImage(i) { VD.idx = i; vdRenderImage(); }
function vdStep(dir) { VD.idx = (VD.idx + dir + VD.imgs.length) % VD.imgs.length; vdRenderImage(); }

function vdShowTab(name, btn) {
    ['overview', 'specs', 'features'].forEach(t => {
        document.getElementById('vd-tab-' + t).style.display = t === name ? 'block' : 'none';
    });
    document.querySelectorAll('.vd-tab').forEach(t => t.classList.remove('active'));
    btn.classList.add('active');
}
