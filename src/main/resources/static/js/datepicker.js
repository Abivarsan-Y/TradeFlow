/* ============================================================
   Date picker for every <input type="date">.

   The calendar of the browser is drawn by the browser itself: it stays tiny on a big screen and cannot
   be styled. This file replaces it with our own calendar, which scales like the rest of the page.

   The original input stays in the page (hidden) with the same id and the value "YYYY-MM-DD", so code
   that reads .value, sets .value = '' (Clear Filters) or listens for "change" keeps working.
   ============================================================ */
(function () {
    const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
                    'July', 'August', 'September', 'October', 'November', 'December'];
    const DAYS = ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];
    const valueProp = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value');

    let pop = null;        // the one calendar window, shared by all fields
    let active = null;     // { input, display, y, m } for the field that is open

    const pad = n => String(n).padStart(2, '0');
    const toIso = (y, m, d) => `${y}-${pad(m + 1)}-${pad(d)}`;
    function parse(v) {
        const p = /^(\d{4})-(\d{2})-(\d{2})$/.exec(v || '');
        return p ? { y: +p[1], m: +p[2] - 1, d: +p[3] } : null;
    }
    function label(v) {
        const p = parse(v);
        return p ? new Date(p.y, p.m, p.d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '';
    }

    /* ---------- the calendar window ---------- */
    function build() {
        pop = document.createElement('div');
        pop.className = 'tj-cal';
        pop.innerHTML = `
            <div class="tj-cal-head">
                <button type="button" class="tj-cal-nav" data-nav="-1" aria-label="Previous month">&#8249;</button>
                <select class="tj-cal-month" aria-label="Month">${MONTHS.map((n, i) => `<option value="${i}">${n}</option>`).join('')}</select>
                <select class="tj-cal-year" aria-label="Year"></select>
                <button type="button" class="tj-cal-nav" data-nav="1" aria-label="Next month">&#8250;</button>
            </div>
            <div class="tj-cal-grid tj-cal-dows">${DAYS.map(d => `<span>${d}</span>`).join('')}</div>
            <div class="tj-cal-grid tj-cal-days"></div>
            <div class="tj-cal-foot">
                <button type="button" data-act="clear">Clear</button>
                <button type="button" data-act="today">Today</button>
            </div>`;
        document.body.appendChild(pop);

        pop.addEventListener('mousedown', e => { if (e.target.tagName !== 'SELECT') e.preventDefault(); });
        pop.addEventListener('click', e => {
            const nav = e.target.closest('[data-nav]');
            const day = e.target.closest('[data-iso]');
            const act = e.target.closest('[data-act]');
            if (nav) { shiftMonth(+nav.dataset.nav); }
            else if (day && !day.disabled) { choose(day.dataset.iso); }
            else if (act && act.dataset.act === 'clear') { choose(''); }
            else if (act && act.dataset.act === 'today') {
                const t = new Date();
                choose(toIso(t.getFullYear(), t.getMonth(), t.getDate()));
            }
        });
        pop.querySelector('.tj-cal-month').addEventListener('change', e => { active.m = +e.target.value; render(); });
        pop.querySelector('.tj-cal-year').addEventListener('change', e => { active.y = +e.target.value; render(); });

        document.addEventListener('mousedown', e => {
            if (active && !pop.contains(e.target) && !active.display.contains(e.target) && e.target !== active.icon) close();
        });
        document.addEventListener('keydown', e => { if (e.key === 'Escape' && active) { const d = active.display; close(); d.focus(); } });
        window.addEventListener('resize', () => { if (active) place(); });
        window.addEventListener('scroll', () => { if (active) place(); }, true);
    }

    function shiftMonth(step) {
        active.m += step;
        if (active.m < 0)  { active.m = 11; active.y--; }
        if (active.m > 11) { active.m = 0;  active.y++; }
        render();
    }

    function render() {
        const { input, y, m } = active;
        const sel = parse(input.value);
        const min = input.getAttribute('min') || '', max = input.getAttribute('max') || '';
        const now = new Date();
        const todayIso = toIso(now.getFullYear(), now.getMonth(), now.getDate());

        pop.querySelector('.tj-cal-month').value = String(m);
        const yearSel = pop.querySelector('.tj-cal-year');
        const first = Math.min(2000, y), last = Math.max(now.getFullYear() + 2, y);
        let opts = '';
        for (let yr = last; yr >= first; yr--) opts += `<option value="${yr}">${yr}</option>`;
        yearSel.innerHTML = opts;
        yearSel.value = String(y);

        const lead = new Date(y, m, 1).getDay();
        const total = new Date(y, m + 1, 0).getDate();
        let cells = '';
        for (let i = 0; i < lead; i++) cells += '<span></span>';
        for (let d = 1; d <= total; d++) {
            const iso = toIso(y, m, d);
            const cls = ['tj-cal-day', iso === todayIso ? 'today' : '', sel && iso === toIso(sel.y, sel.m, sel.d) ? 'selected' : ''].join(' ').trim();
            const off = (min && iso < min) || (max && iso > max) ? ' disabled' : '';
            cells += `<button type="button" class="${cls}" data-iso="${iso}"${off}>${d}</button>`;
        }
        pop.querySelector('.tj-cal-days').innerHTML = cells;
    }

    function place() {
        const r = active.display.getBoundingClientRect();
        const w = pop.offsetWidth, h = pop.offsetHeight, gap = 6;
        let left = Math.min(Math.max(8, r.left), window.innerWidth - w - 8);
        let top = r.bottom + gap;
        if (top + h > window.innerHeight - 8 && r.top - gap - h > 8) top = r.top - gap - h;   // not enough room below: open above
        pop.style.left = left + 'px';
        pop.style.top = top + 'px';
    }

    function open(input, display, icon) {
        if (!pop) build();
        const sel = parse(input.value);
        const t = new Date();
        active = { input, display, icon, y: sel ? sel.y : t.getFullYear(), m: sel ? sel.m : t.getMonth() };
        render();
        pop.classList.add('open');
        place();
    }

    function close() {
        if (pop) pop.classList.remove('open');
        active = null;
    }

    function choose(iso) {
        const input = active.input, display = active.display;
        close();
        valueProp.set.call(input, iso);
        display.value = label(iso);
        input.dispatchEvent(new Event('input',  { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
    }

    /* ---------- replace one <input type="date"> ---------- */
    function enhance(input) {
        if (input.dataset.tjDate) return;
        input.dataset.tjDate = '1';

        const wrap = document.createElement('div');
        wrap.className = 'tj-date';
        const display = document.createElement('input');
        display.type = 'text';
        display.readOnly = true;
        display.autocomplete = 'off';
        display.className = (input.className ? input.className + ' ' : '') + 'tj-date-display';
        display.placeholder = input.getAttribute('placeholder') || 'Select date';
        display.title = input.title || '';
        const icon = document.createElement('i');
        icon.className = 'bi bi-calendar3 tj-date-icon';
        wrap.appendChild(display);
        wrap.appendChild(icon);
        input.parentNode.insertBefore(wrap, input);

        const initial = input.value;
        input.type = 'hidden';
        valueProp.set.call(input, initial);
        display.value = label(initial);

        // when other code sets input.value (for example Clear Filters), show it
        Object.defineProperty(input, 'value', {
            configurable: true,
            get() { return valueProp.get.call(this); },
            set(v) { valueProp.set.call(this, v); display.value = label(v); }
        });

        const toggle = () => { if (active && active.input === input) close(); else open(input, display, icon); };
        display.addEventListener('click', toggle);
        icon.style.pointerEvents = 'auto';
        icon.addEventListener('click', toggle);
        display.addEventListener('keydown', e => {
            if (e.key === 'Enter' || e.key === ' ' || e.key === 'ArrowDown') { e.preventDefault(); if (!(active && active.input === input)) open(input, display, icon); }
            if (e.key === 'Backspace' || e.key === 'Delete') { e.preventDefault(); active = { input, display }; choose(''); }
        });
    }

    function enhanceAll(root) {
        (root || document).querySelectorAll('input[type="date"]').forEach(enhance);
    }

    document.addEventListener('DOMContentLoaded', () => {
        enhanceAll(document);
        // fields that a script adds to the page later
        new MutationObserver(muts => muts.forEach(mu => mu.addedNodes.forEach(n => {
            if (n.nodeType === 1) { if (n.matches && n.matches('input[type="date"]')) enhance(n); else enhanceAll(n); }
        }))).observe(document.body, { childList: true, subtree: true });
    });
})();
