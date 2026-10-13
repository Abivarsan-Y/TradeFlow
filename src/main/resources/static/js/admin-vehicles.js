/* ================================================
   TRADE JAPAN — Admin: Vehicles
   Add / Edit form has three tabs: Overview, Specification (18 required fields), Features (28 on/off)
   Uses apiFetch / showToast / showConfirm / escapeHtml from the dashboard + app.js, tjErr from orders.js
   ================================================ */

/* the 18 specifications (key = field name sent to the API) */
const VEHICLE_SPECS = [
    { key: 'refNo',             label: 'Ref No',          type: 'text',   ph: 'e.g. TJ00006' },
    { key: 'category',          label: 'Body Type',       type: 'select', options: ['Sedan', 'SUV', 'Hatchback', 'Van', 'Pickup', 'Coupe', 'Wagon'] },
    { key: 'vin',               label: 'Chassis No',      type: 'text',   ph: 'e.g. MXPJ10-3147779' },
    { key: 'modelCode',         label: 'Model Code',      type: 'text',   ph: 'e.g. 6AA-MXPJ10' },
    { key: 'grade',             label: 'Grade',           type: 'text',   ph: 'e.g. 1.5 Hybrid G' },
    { key: 'engineType',        label: 'Engine Model',    type: 'text',   ph: 'e.g. M15A-FXE' },
    { key: 'mileage',           label: 'Odometer (km)',   type: 'number', min: 0 },
    { key: 'displacementCc',    label: 'Displacement (cc)', type: 'number', min: 1 },
    { key: 'steering',          label: 'Steering',        type: 'select', options: ['Right', 'Left'] },
    { key: 'transmission',      label: 'Transmission',    type: 'select', options: ['Automatic', 'Manual', 'CVT'] },
    { key: 'doors',             label: 'Doors',           type: 'number', min: 1 },
    { key: 'passengers',        label: 'Passengers',      type: 'number', min: 1 },
    { key: 'dimension',         label: 'Dimension',       type: 'text',   ph: 'e.g. 4,490 x 1,825 x 1,620 mm' },
    { key: 'size',              label: 'Size',            type: 'text',   ph: 'e.g. Mid-size' },
    { key: 'exteriorColor',     label: 'Exterior Color',  type: 'text',   ph: 'e.g. White' },
    { key: 'driveType',         label: 'Drive Type',      type: 'select', options: ['2WD', '4WD', 'AWD'] },
    { key: 'fuelType',          label: 'Fuel Type',       type: 'select', options: ['Petrol', 'Diesel', 'Hybrid', 'Electric'] },
    { key: 'inventoryLocation', label: 'Location',        type: 'select', options: ['Japan', 'UK'] }
];

let allVehicles = [];
let vehiclePage = 1;
const VEHICLES_PER_PAGE = 8;
let vehicleSort = { key: 'added', dir: 'desc' };   // newest first by default
let featureCatalog = [];
let vehicleFormBuilt = false;

const statusLabel = s => s ? s.charAt(0) + s.slice(1).toLowerCase() : '';

/* what each sortable column sorts by */
const VEHICLE_SORTERS = {
    name:      v => `${v.make} ${v.model} ${v.category || ''}`.toLowerCase(),
    makeModel: v => `${v.make} ${v.model}`.toLowerCase(),
    year:      v => Number(v.manufactureYear || 0),
    price:     v => Number(v.fobPrice || 0),
    status:    v => (v.status || '').toLowerCase(),
    added:     v => v.createdAt ? new Date(v.createdAt).getTime() : 0
};

/* ======================= LIST ======================= */
async function loadVehicles() {
    const res = await apiFetch('/vehicles?page=0&size=500');
    const tbody = document.getElementById('vehicles-list');
    if (res.ok && Array.isArray(res.data)) {
        allVehicles = res.data;
        fillVehicleFilters();
        renderVehicles();
    } else {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:32px;color:var(--tj-text-muted);">Could not load vehicles.</td></tr>';
    }
}

async function refreshVehicles() {
    await loadVehicles();
    showToast('Vehicle list refreshed.');
}

/* fill a filter <select> from the real data, keeping whatever is currently chosen */
function fillFilterSelect(id, values, firstLabel, descending) {
    const el = document.getElementById(id);
    const current = el.value;
    const list = [...new Set(values.filter(v => v !== null && v !== undefined && v !== '').map(String))];
    list.sort((a, b) => a.localeCompare(b, undefined, { numeric: true }));
    if (descending) list.reverse();
    el.innerHTML = `<option value="">${firstLabel}</option>` + list.map(v => `<option value="${escapeHtml(v)}">${escapeHtml(v)}</option>`).join('');
    el.value = list.includes(current) ? current : '';
}

function fillVehicleFilters() {
    const make = document.getElementById('vf-make').value;
    fillFilterSelect('vf-make', allVehicles.map(v => v.make), 'All Makes');
    fillFilterSelect('vf-model', allVehicles.filter(v => !make || v.make === make).map(v => v.model), 'All Models');
    fillFilterSelect('vf-year', allVehicles.map(v => v.manufactureYear), 'All Years', true);
}

function vehicleMakeChanged() {
    document.getElementById('vf-model').value = '';
    const make = document.getElementById('vf-make').value;
    fillFilterSelect('vf-model', allVehicles.filter(v => !make || v.make === make).map(v => v.model), 'All Models');
    vehicleFiltersChanged();
}

function vehicleFiltersChanged() {
    vehiclePage = 1;
    renderVehicles();
}

function sortVehicles(key) {
    if (vehicleSort.key === key) vehicleSort.dir = vehicleSort.dir === 'asc' ? 'desc' : 'asc';
    else vehicleSort = { key, dir: key === 'added' ? 'desc' : 'asc' };
    vehiclePage = 1;
    renderVehicles();
}

function updateSortIcons() {
    document.querySelectorAll('#section-vehicles th.sortable').forEach(th => {
        const icon = th.querySelector('.sort-icon');
        const active = th.dataset.sort === vehicleSort.key;
        icon.className = 'bi sort-icon ' + (active ? (vehicleSort.dir === 'asc' ? 'bi-arrow-up' : 'bi-arrow-down') : 'bi-arrow-down-up');
        icon.style.opacity = active ? '1' : '.5';
    });
}

/* apply search + filters, then sort */
function getFilteredVehicles() {
    const val = id => document.getElementById(id).value.trim();
    const q = val('vf-search').toLowerCase();
    const make = val('vf-make'), model = val('vf-model'), year = val('vf-year'), status = val('vf-status');
    const min = val('vf-min'), max = val('vf-max');

    const list = allVehicles.filter(v => {
        const name = `${v.make} ${v.model} ${v.category || ''}`.toLowerCase();
        const ref = (v.refNo || '').toLowerCase();
        return (!q || name.includes(q) || ref.includes(q))
            && (!make || v.make === make)
            && (!model || v.model === model)
            && (!year || String(v.manufactureYear) === year)
            && (!status || (v.status || '').toUpperCase() === status)
            && (min === '' || Number(v.fobPrice) >= Number(min))
            && (max === '' || Number(v.fobPrice) <= Number(max));
    });

    const get = VEHICLE_SORTERS[vehicleSort.key];
    const dir = vehicleSort.dir === 'asc' ? 1 : -1;
    list.sort((a, b) => { const x = get(a), y = get(b); return (x < y ? -1 : x > y ? 1 : 0) * dir; });
    return list;
}

function renderVehicles() {
    const tbody = document.getElementById('vehicles-list');
    const footer = document.getElementById('vehicles-pagination');
    const list = getFilteredVehicles();
    const total = list.length;
    updateSortIcons();

    if (!total) {
        const msg = allVehicles.length ? 'No vehicles match your search or filters.' : 'No vehicles yet. Click "Add Vehicle" to create one.';
        tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:32px;color:var(--tj-text-muted);">${msg}</td></tr>`;
        footer.innerHTML = '<span class="pager-text">Showing 0 vehicles</span>';
        return;
    }

    const pages = Math.ceil(total / VEHICLES_PER_PAGE);
    if (vehiclePage > pages) vehiclePage = pages;
    const start = (vehiclePage - 1) * VEHICLES_PER_PAGE;
    const rows = list.slice(start, start + VEHICLES_PER_PAGE);

    tbody.innerHTML = rows.map(v => {
        const img = v.imageUrls ? v.imageUrls.split(',')[0].trim() : '';
        const st = (v.status || 'AVAILABLE').toUpperCase();
        const added = v.createdAt ? new Date(v.createdAt).toLocaleDateString('en-US') : '—';
        return `
            <tr>
                <td><div class="vehicle-thumb">${img ? `<img src="${escapeHtml(img)}" alt="">` : '<i class="bi bi-car-front"></i>'}</div></td>
                <td>
                    <div class="vt-name">${escapeHtml(v.make || '')} ${escapeHtml(v.model || '')} ${escapeHtml(v.category || '')}</div>
                    <div class="vt-sub">Ref No: ${escapeHtml(v.refNo || ('TJ' + v.id))}</div>
                </td>
                <td>
                    <div>${escapeHtml(v.make || '')}</div>
                    <div class="vt-sub">${escapeHtml(v.model || '')}</div>
                </td>
                <td>${escapeHtml(String(v.manufactureYear ?? ''))}</td>
                <td class="vt-price">$${Number(v.fobPrice || 0).toLocaleString('en-US')}</td>
                <td>
                    <select class="status-select" data-prev="${st}" onchange="confirmVehicleStatus(${v.id}, this)">
                        <option value="AVAILABLE" ${st === 'AVAILABLE' ? 'selected' : ''}>Available</option>
                        <option value="RESERVED"  ${st === 'RESERVED' ? 'selected' : ''}>Reserved</option>
                        <option value="SOLD"      ${st === 'SOLD' ? 'selected' : ''}>Sold</option>
                    </select>
                </td>
                <td>${added}</td>
                <td>
                    <div style="display:flex;gap:8px;align-items:center;">
                        <button class="action-btn" title="Edit" onclick="openVehicleModal(${v.id})"><i class="bi bi-pencil"></i></button>
                        <button class="action-btn delete" title="Delete" onclick="deleteVehicle(${v.id})"><i class="bi bi-trash"></i></button>
                    </div>
                </td>
            </tr>`;
    }).join('');

    // footer: text on the left; page buttons on the right ONLY when there is more than one page
    let pager = '';
    if (pages > 1) {
        let btns = '';
        for (let i = 1; i <= pages; i++) btns += `<button class="page-btn ${i === vehiclePage ? 'active' : ''}" onclick="goVehiclePage(${i})">${i}</button>`;
        pager = `
            <button class="page-btn" ${vehiclePage === 1 ? 'disabled' : ''} onclick="goVehiclePage(${vehiclePage - 1})"><i class="bi bi-chevron-left"></i></button>
            ${btns}
            <button class="page-btn" ${vehiclePage === pages ? 'disabled' : ''} onclick="goVehiclePage(${vehiclePage + 1})"><i class="bi bi-chevron-right"></i></button>`;
    }
    footer.innerHTML = `<span class="pager-text">Showing ${start + 1} to ${start + rows.length} of ${total} vehicles</span>${pager}`;
}

function goVehiclePage(n) { vehiclePage = n; renderVehicles(); }

/* ======================= STATUS (asks for confirmation) ======================= */
function confirmVehicleStatus(id, select) {
    const prev = select.dataset.prev;
    const next = select.value;
    if (prev === next) return;
    const v = allVehicles.find(x => x.id === id);
    const name = v ? `${v.make} ${v.model} ${v.manufactureYear}` : 'this vehicle';

    showConfirm(
        `Change the status of ${name} from “${statusLabel(prev)}” to “${statusLabel(next)}”?`,
        async () => {
            const res = await apiFetch(`/admin/vehicles/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status: next }) });
            if (res.ok) {
                select.dataset.prev = next;
                if (v) v.status = next;
                showToast(`Status changed to ${statusLabel(next)}.`);
            } else {
                select.value = prev;
                showToast(tjErr(res, 'Failed to update status.'), true);
            }
        },
        () => { select.value = prev; }   // cancelled -> put the old value back
    );
}

/* ======================= DELETE ======================= */
function deleteVehicle(id) {
    const v = allVehicles.find(x => x.id === id);
    showConfirm(`Delete ${v ? v.make + ' ' + v.model : 'this vehicle'} permanently?`, async () => {
        const res = await apiFetch(`/admin/vehicles/${id}`, { method: 'DELETE' });
        if (res.ok) { showToast('Vehicle deleted.'); loadVehicles(); }
        else showToast(tjErr(res, 'Failed to delete vehicle.'), true);
    });
}

/* ======================= ADD / EDIT FORM ======================= */
async function loadFeatureCatalog() {
    if (featureCatalog.length) return;
    const res = await apiFetch('/features');
    if (res.ok && Array.isArray(res.data)) featureCatalog = res.data;
}

function buildVehicleForm() {
    if (vehicleFormBuilt) return;
    // --- 18 specification fields ---
    document.getElementById('v-spec-fields').innerHTML = VEHICLE_SPECS.map(f => {
        let input;
        if (f.type === 'select') {
            input = `<select class="form-control" id="spec-${f.key}"><option value="">Select...</option>${f.options.map(o => `<option>${o}</option>`).join('')}</select>`;
        } else if (f.type === 'number') {
            input = `<input type="number" class="form-control" id="spec-${f.key}" min="${f.min}" step="1">`;
        } else {
            input = `<input type="text" class="form-control" id="spec-${f.key}" placeholder="${f.ph || ''}">`;
        }
        return `<div class="col-md-4"><label class="form-label">${f.label} <span class="required">*</span></label>${input}</div>`;
    }).join('');

    // --- 28 feature toggles ---
    document.getElementById('v-feature-list').innerHTML = featureCatalog.length
        ? featureCatalog.map(f => `
            <label class="feature-toggle">
                <span class="feature-name">${escapeHtml(f.name)}</span>
                <span class="toggle-switch"><input type="checkbox" data-fid="${f.featureId}" data-name="${escapeHtml(f.name)}" onchange="updateFeatureCount()"><span class="slider"></span></span>
            </label>`).join('')
        : '<p class="text-muted">Could not load the feature list.</p>';

    // clear red highlight as soon as the admin types
    document.querySelectorAll('#vehicleModal .form-control').forEach(el => {
        el.addEventListener('input', () => el.classList.remove('is-invalid'));
        el.addEventListener('change', () => el.classList.remove('is-invalid'));
    });
    vehicleFormBuilt = true;
}

function setFieldValue(el, value) {
    const v = value == null ? '' : String(value);
    if (el.tagName === 'SELECT' && v && ![...el.options].some(o => o.value === v || o.text === v)) {
        el.add(new Option(v, v));   // keep an older value that is not in the standard list
    }
    el.value = v;
}

function nextRefNo() {
    let max = 0;
    allVehicles.forEach(v => {
        const n = parseInt(String(v.refNo || '').replace(/\D/g, ''), 10);
        if (!isNaN(n) && n > max) max = n;
    });
    return 'TJ' + String(max + 1).padStart(5, '0');
}

function showVehicleTab(name, btn) {
    ['overview', 'spec', 'features'].forEach(t => {
        document.getElementById('vtab-' + t).style.display = t === name ? 'block' : 'none';
    });
    document.querySelectorAll('#v-tabs .tab-item').forEach(t => t.classList.remove('active'));
    (btn || document.querySelector(`#v-tabs .tab-item[data-tab="${name}"]`)).classList.add('active');
}

function setAllFeatures(on) {
    document.querySelectorAll('#v-feature-list input[type=checkbox]').forEach(cb => cb.checked = on);
    updateFeatureCount();
}

function updateFeatureCount() {
    const boxes = document.querySelectorAll('#v-feature-list input[type=checkbox]');
    const on = [...boxes].filter(b => b.checked).length;
    document.getElementById('v-feature-count').textContent = `${on} of ${boxes.length} features ON`;
}

async function openVehicleModal(id) {
    await loadFeatureCatalog();
    buildVehicleForm();

    const v = id ? allVehicles.find(x => x.id === id) : null;
    document.getElementById('vehicleModalTitle').textContent = v ? 'Edit Vehicle' : 'Add Vehicle';
    document.getElementById('v-id').value = v ? v.id : '';

    // Overview tab
    document.getElementById('v-make').value     = v ? (v.make || '') : '';
    document.getElementById('v-model').value    = v ? (v.model || '') : '';
    document.getElementById('v-year').value     = v ? v.manufactureYear : '';
    document.getElementById('v-price').value    = v ? v.fobPrice : '';
    setFieldValue(document.getElementById('v-condition'), v && v.condition ? v.condition : '');
    setFieldValue(document.getElementById('v-status'), v && v.status ? v.status : 'AVAILABLE');
    document.getElementById('v-images').value   = v ? (v.imageUrls || '') : '';
    document.getElementById('v-overview').value = v ? (v.description || '') : '';

    // Specification tab
    VEHICLE_SPECS.forEach(f => {
        const el = document.getElementById('spec-' + f.key);
        setFieldValue(el, v ? v[f.key] : '');
    });
    if (!v) document.getElementById('spec-refNo').value = nextRefNo();   // suggested, still editable

    // Features tab
    const on = new Set(v ? (v.features || []).map(f => f.name) : []);
    document.querySelectorAll('#v-feature-list input[type=checkbox]').forEach(cb => cb.checked = on.has(cb.dataset.name));
    updateFeatureCount();

    document.querySelectorAll('#vehicleModal .is-invalid').forEach(el => el.classList.remove('is-invalid'));
    document.getElementById('vehicleError').style.display = 'none';
    showVehicleTab('overview');
    bootstrap.Modal.getOrCreateInstance(document.getElementById('vehicleModal')).show();
}

async function saveVehicle() {
    const errEl = document.getElementById('vehicleError');
    const problems = [];   // {label, tab, el}

    const check = (el, label, tab, isNumber, min) => {
        const val = el.value.trim();
        let bad = val === '';
        if (!bad && isNumber) bad = isNaN(Number(val)) || Number(val) < min;
        el.classList.toggle('is-invalid', bad);
        if (bad) problems.push({ label, tab });
    };

    // Overview tab
    check(document.getElementById('v-make'), 'Make', 'overview');
    check(document.getElementById('v-model'), 'Model', 'overview');
    check(document.getElementById('v-year'), 'Manufacture Year', 'overview', true, 1950);
    check(document.getElementById('v-price'), 'FOB Price', 'overview', true, 0.01);
    check(document.getElementById('v-condition'), 'Condition', 'overview');
    check(document.getElementById('v-overview'), 'Overview', 'overview');
    // Specification tab (all 18)
    VEHICLE_SPECS.forEach(f => check(document.getElementById('spec-' + f.key), f.label.replace(/ \(.*\)/, ''), 'spec', f.type === 'number', f.min));

    if (problems.length) {
        errEl.textContent = 'Please fill in every required field: ' + problems.map(p => p.label).join(', ') + '.';
        errEl.style.display = 'block';
        showVehicleTab(problems[0].tab);   // jump to the first tab that has a problem
        return;
    }

    const payload = {
        make: document.getElementById('v-make').value.trim(),
        model: document.getElementById('v-model').value.trim(),
        manufactureYear: Number(document.getElementById('v-year').value),
        fobPrice: Number(document.getElementById('v-price').value),
        condition: document.getElementById('v-condition').value,
        status: document.getElementById('v-status').value,
        imageUrls: document.getElementById('v-images').value.trim(),
        description: document.getElementById('v-overview').value.trim(),
        featureIds: [...document.querySelectorAll('#v-feature-list input[type=checkbox]:checked')].map(cb => Number(cb.dataset.fid))
    };
    VEHICLE_SPECS.forEach(f => {
        const val = document.getElementById('spec-' + f.key).value.trim();
        payload[f.key] = f.type === 'number' ? Number(val) : val;
    });

    const id = document.getElementById('v-id').value;
    const res = await apiFetch(id ? `/admin/vehicles/${id}` : '/admin/vehicles', {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify(payload)
    });
    if (res.ok) {
        bootstrap.Modal.getInstance(document.getElementById('vehicleModal')).hide();
        showToast(id ? 'Vehicle updated successfully!' : 'Vehicle added successfully!');
        loadVehicles();
    } else if (res.status !== 401 && res.status !== 403) {
        errEl.textContent = tjErr(res, 'Failed to save vehicle.');
        errEl.style.display = 'block';
    }
}
