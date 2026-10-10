const API = "/api/vehicles";
const TEAM_EMAIL = "";

function parseImageUrls(raw) {
    if (!raw) {
        return [];
    }
    return raw
        .split(",")
        .map((url) => url.trim())
        .filter((url) => url.length > 0)
        .slice(0, 12);
}

function formatUsd(value) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 0
    }).format(value);
}

/* ── Catalogue Card ────────────────────────────────────────── */
function createCard(vehicle) {
    const images = parseImageUrls(vehicle.imageUrls);
    const hero = images[0] || "https://picsum.photos/id/111/800/500";

    const isNew = vehicle.createdAt && (new Date() - new Date(vehicle.createdAt)) / (1000 * 60 * 60 * 24) <= 14;
    const badgeHtml = isNew ? `<span class="badge bg-danger position-absolute m-2" style="top: 0; left: 0; z-index: 10;">New Arrival</span>` : "";

    const article = document.createElement("article");
    article.className = "card";
    article.setAttribute("role", "link");
    article.tabIndex = 0;
    article.innerHTML = `
        <div class="card-img-wrapper position-relative">
            ${badgeHtml}
            <img class="hero" src="${hero}" alt="${vehicle.make} ${vehicle.model}">
        </div>
        <div class="card-body">
            <h2>${vehicle.make} ${vehicle.model} ${vehicle.manufactureYear}</h2>
            <p class="price">$USD ${Number(vehicle.fobPrice).toLocaleString()}</p>
        </div>
    `;

    const openDetails = () => { window.location.href = `/vehicle-detail.html?id=${vehicle.id}`; };
    article.addEventListener("click", openDetails);
    article.addEventListener("keydown", (event) => {
        if (event.target === article && (event.key === "Enter" || event.key === " ")) {
            event.preventDefault();
            openDetails();
        }
    });
    return article;
}

/* ── Auth Navigation ───────────────────────────────────────── */
function renderAuthenticationNavigation() {
    const authActions = document.getElementById("auth-actions");
    if (!authActions) return;

    if (localStorage.getItem("token")) {
        authActions.innerHTML = `
            <a href="account.html"><i class="bi bi-person-circle"></i></a>
            <button class="nav-button" id="logout-button" type="button">Logout</button>`;
        document.getElementById("logout-button").addEventListener("click", () => {
            localStorage.removeItem("token");
            window.location.reload();
        });
    } else {
        authActions.innerHTML = `
            <a href="login.html">Login</a>
            <a href="register.html">Register</a>`;
    }
}

/* ── Catalogue Loader ──────────────────────────────────────── */
let currentPage = 0;
let isLastPage = false;
let isLoading = false;

async function loadCatalogue(reset = true) {
    if (isLoading) return;
    if (reset) {
        currentPage = 0;
        isLastPage = false;
    }
    if (isLastPage) return;

    isLoading = true;
    const loadingIndicator = document.getElementById("loading-indicator");
    if (loadingIndicator) loadingIndicator.hidden = false;

    const form = document.getElementById("filter-form");
    if (!form) {
        isLoading = false;
        if (loadingIndicator) loadingIndicator.hidden = true;
        return;
    }
    const params = new URLSearchParams();
    ["make", "model", "category", "location", "status", "maxPrice", "minYear"].forEach((name) => {
        const el = form.elements[name];
        if (el) {
            const value = el.value.trim();
            if (value) {
                params.set(name, value);
            }
        }
    });
    params.set("page", currentPage);
    params.set("size", 10);
    const query = params.toString();
    const response = await fetch(query ? `${API}?${query}` : API);
    if (!response.ok) {
        isLoading = false;
        if (loadingIndicator) loadingIndicator.hidden = true;
        throw new Error("Failed to load vehicles");
    }
    const vehicles = await response.json();

    const results = document.getElementById("results");
    const empty = document.getElementById("empty");
    const count = document.getElementById("vehicle-count");

    if (reset) {
        results.innerHTML = "";
        if (count) count.textContent = `Vehicles Found (Showing ${vehicles.length})`;
    } else {
        if (count) count.textContent = `Vehicles Found (Showing ${results.children.length + vehicles.length})`;
    }

    if (vehicles.length === 0 && reset) {
        empty.hidden = false;
        isLastPage = true;
        isLoading = false;
        if (loadingIndicator) loadingIndicator.hidden = true;
        return;
    } else if (vehicles.length < 10) {
        isLastPage = true;
    }

    empty.hidden = true;
    vehicles.forEach((vehicle) => results.appendChild(createCard(vehicle)));

    currentPage++;
    isLoading = false;
    if (loadingIndicator) loadingIndicator.hidden = true;
}

/* ── Admin Inventory ───────────────────────────────────────── */
let allAdminVehicles = [];
let adminPage = 1;
const ADMIN_PAGE_SIZE = 6;

async function loadAdminInventory() {
    const body = document.getElementById("admin-inventory");
    if (!body) return;
    const message = document.getElementById("inventory-msg");
    const response = await fetch(API);
    if (!response.ok) throw new Error("Failed to load inventory");
    allAdminVehicles = await response.json();
    adminPage = 1;
    renderAdminPage();
}

function renderAdminPage() {
    const body = document.getElementById("admin-inventory");
    const message = document.getElementById("inventory-msg");
    const totalVehicles = allAdminVehicles.length;
    const totalPages = Math.max(1, Math.ceil(totalVehicles / ADMIN_PAGE_SIZE));
    if (adminPage > totalPages) adminPage = totalPages;
    const start = (adminPage - 1) * ADMIN_PAGE_SIZE;
    const end = Math.min(start + ADMIN_PAGE_SIZE, totalVehicles);
    const pageVehicles = allAdminVehicles.slice(start, end);

    body.innerHTML = pageVehicles.map((vehicle) => {
        const images = parseImageUrls(vehicle.imageUrls);
        const thumb = images[0] || "https://picsum.photos/id/111/70/50";
        const refNo = `TJ${String(vehicle.id).padStart(5, "0")}`;
        return `
        <tr>
            <td><img class="table-vehicle-photo" src="${thumb}" alt="${vehicle.make} ${vehicle.model}"></td>
            <td>
                <span class="table-vehicle-name">${vehicle.make} ${vehicle.model} ${vehicle.category}</span>
                <span class="table-vehicle-ref">Ref No: ${refNo}</span>
            </td>
            <td>
                <span class="table-vehicle-make">${vehicle.make}</span>
                <span class="table-vehicle-model">${vehicle.model}</span>
            </td>
            <td>${vehicle.manufactureYear}</td>
            <td><span class="table-price">${formatUsd(vehicle.fobPrice)}</span></td>
            <td>
                <select class="status-select" data-id="${vehicle.id}" aria-label="Status for ${vehicle.make} ${vehicle.model}">
                    ${["Available", "Reserved", "Sold"].map((status) =>
                        `<option ${status === vehicle.status ? "selected" : ""}>${status}</option>`
                    ).join("")}
                </select>
            </td>
            <td>${vehicle.createdAt ? new Date(vehicle.createdAt).toLocaleDateString() : 'N/A'}</td>
            <td>
                <div class="table-actions">
                    <button class="action-btn" type="button" title="Edit"><i class="bi bi-pencil"></i></button>
                    <button class="action-btn delete-btn" type="button" data-id="${vehicle.id}" title="Delete"><i class="bi bi-trash"></i></button>
                </div>
            </td>
        </tr>`;
    }).join("");

    // Pagination info
    const paginationInfo = document.getElementById("pagination-info");
    if (paginationInfo) {
        paginationInfo.textContent = totalVehicles > 0
            ? `Showing ${start + 1} to ${end} of ${totalVehicles} vehicles`
            : "No vehicles found";
    }

    // Pagination controls
    const paginationControls = document.getElementById("pagination-controls");
    if (paginationControls) {
        let html = `<button class="page-btn" ${adminPage <= 1 ? "disabled" : ""} data-page="${adminPage - 1}">‹</button>`;
        for (let i = 1; i <= totalPages; i++) {
            html += `<button class="page-btn ${i === adminPage ? "active" : ""}" data-page="${i}">${i}</button>`;
        }
        html += `<button class="page-btn" ${adminPage >= totalPages ? "disabled" : ""} data-page="${adminPage + 1}">›</button>`;
        paginationControls.innerHTML = html;
        paginationControls.querySelectorAll(".page-btn").forEach((btn) => {
            btn.addEventListener("click", () => {
                const page = Number(btn.dataset.page);
                if (page >= 1 && page <= totalPages) {
                    adminPage = page;
                    renderAdminPage();
                }
            });
        });
    }

    // Status change handlers
    body.querySelectorAll(".status-select").forEach((select) => {
        select.addEventListener("change", async () => {
            const previous = select.dataset.previous || select.value;
            select.disabled = true;
            try {
                const update = await fetch(`/api/admin/vehicles/${select.dataset.id}`, {
                    method: "PUT", headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ status: select.value })
                });
                if (!update.ok) throw new Error("Status update failed");
                select.dataset.previous = select.value;
                const message = document.getElementById("inventory-msg");
                message.hidden = false; message.className = "msg ok";
                message.textContent = "Vehicle status updated.";
            } catch (error) {
                select.value = previous;
                const message = document.getElementById("inventory-msg");
                message.hidden = false; message.className = "msg err";
                message.textContent = error.message;
            } finally { select.disabled = false; }
        });
    });

    // Delete handlers
    body.querySelectorAll(".delete-btn").forEach((button) => {
        button.addEventListener("click", async () => {
            const row = button.closest("tr");
            const vehicleName = row.querySelector(".table-vehicle-name").textContent;
            if (!window.confirm(`Permanently delete ${vehicleName} from inventory?`)) return;

            button.disabled = true;
            try {
                const deletion = await fetch(`/api/admin/vehicles/${button.dataset.id}`, { method: "DELETE" });
                if (!deletion.ok) throw new Error(`Could not delete vehicle (${deletion.status})`);
                const message = document.getElementById("inventory-msg");
                message.hidden = false; message.className = "msg ok";
                message.textContent = `${vehicleName} was deleted from inventory.`;
                await loadAdminInventory();
            } catch (error) {
                const message = document.getElementById("inventory-msg");
                message.hidden = false; message.className = "msg err";
                message.textContent = error.message;
                button.disabled = false;
            }
        });
    });
}

/* ── Vehicle Detail ────────────────────────────────────────── */
async function loadVehicleDetails() {
    const detail = document.getElementById("vehicle-detail");
    if (!detail) return;
    const id = new URLSearchParams(window.location.search).get("id");
    if (!id) throw new Error("No vehicle selected");
    const response = await fetch(`${API}/${encodeURIComponent(id)}`);
    if (!response.ok) throw new Error("Vehicle not found");
    const vehicle = await response.json();
    const images = parseImageUrls(vehicle.imageUrls);
    const hero = images[0] || "https://picsum.photos/id/111/800/500";
    const title = `${vehicle.make} ${vehicle.model} ${vehicle.manufactureYear}`;
    document.title = `Trade Japan — ${title}`;

    // Update breadcrumb
    const breadcrumbName = document.getElementById("breadcrumb-name");
    if (breadcrumbName) breadcrumbName.textContent = title;

    const thumbsHtml = images.map((src, index) =>
        `<img src="${src}" data-src="${src}" class="${index === 0 ? "active" : ""}" alt="">`
    ).join("");

    detail.innerHTML = `
        <!-- Top: Gallery + Info -->
        <div class="detail-top">
            <div class="detail-gallery">
                <div class="detail-hero-wrapper">
                    <span class="badge-label">${vehicle.status === "Sold" ? "Sold" : "Used Vehicle"}</span>
                    <span class="image-counter" id="image-counter">${images.length > 0 ? "1 / " + images.length : ""}</span>
                    <img id="detail-hero" class="detail-hero" src="${hero}" alt="${title}">
                </div>
                <div class="detail-thumbs">${thumbsHtml}</div>
            </div>
            <div class="detail-info">
                <h1>${title}</h1>
                <p class="price">$USD ${Number(vehicle.fobPrice).toLocaleString()}</p>
                <div class="detail-specs-grid">
                    <div class="spec-item">
                        <i class="bi bi-speedometer2"></i>
                        <div class="spec-text">
                            <span class="spec-label">Mileage</span>
                            <span class="spec-value">${Number(vehicle.mileage).toLocaleString()} km</span>
                        </div>
                    </div>
                    <div class="spec-item">
                        <i class="bi bi-calendar3"></i>
                        <div class="spec-text">
                            <span class="spec-label">Year</span>
                            <span class="spec-value">${vehicle.manufactureYear}</span>
                        </div>
                    </div>
                    <div class="spec-item">
                        <i class="bi bi-gear"></i>
                        <div class="spec-text">
                            <span class="spec-label">Engine</span>
                            <span class="spec-value">${vehicle.engineType}</span>
                        </div>
                    </div>
                    <div class="spec-item">
                        <i class="bi bi-geo-alt"></i>
                        <div class="spec-text">
                            <span class="spec-label">Location</span>
                            <span class="spec-value">${vehicle.inventoryLocation}</span>
                        </div>
                    </div>
                </div>
                <a class="btn-inquire contact-link email" href="${emailUrl(vehicle)}" target="_blank" rel="noopener">
                    <i class="bi bi-envelope"></i> Inquire Now
                </a>
                <a class="btn-whatsapp contact-link whatsapp" href="${whatsappUrl(vehicle)}" target="_blank" rel="noopener">
                    <i class="bi bi-whatsapp"></i> WhatsApp
                </a>
            </div>
        </div>

        <!-- Tabs Section -->
        <div class="detail-tabs">
            <div class="tab-nav">
                <button class="tab-btn active" data-tab="overview">Overview</button>
                <button class="tab-btn" data-tab="specifications">Specifications</button>
                <button class="tab-btn" data-tab="features">Features</button>
            </div>
            <div class="tab-content">
                <div class="tab-pane active" id="tab-overview">
                    <p>The ${title} combines outstanding reliability with advanced technology.
                    Built for durability and performance, it offers a powerful ${vehicle.engineType} engine,
                    a comfortable and spacious interior, and the latest safety features.
                    Whether for family use or adventure, this vehicle delivers unmatched reliability
                    on and off the road.</p>
                </div>
                <div class="tab-pane" id="tab-specifications">
                    <table style="width:100%;border-collapse:collapse;">
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);width:40%;">Make</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.make}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Model</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.model}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Year</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.manufactureYear}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Engine</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.engineType}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Mileage</td><td style="padding:0.5rem 0;font-weight:600;">${Number(vehicle.mileage).toLocaleString()} km</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Category</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.category}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Location</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.inventoryLocation}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">VIN</td><td style="padding:0.5rem 0;font-weight:600;">${vehicle.vin}</td></tr>
                        <tr><td style="padding:0.5rem 0;color:var(--tj-text-muted);">Status</td><td style="padding:0.5rem 0;font-weight:600;"><span class="badge ${vehicle.status}">${vehicle.status}</span></td></tr>
                    </table>
                </div>
                <div class="tab-pane" id="tab-features">
                    <ul style="padding-left:1.25rem;line-height:2;">
                        <li>${vehicle.engineType} Engine</li>
                        <li>${vehicle.category} Body Type</li>
                        <li>${vehicle.inventoryLocation} Inventory</li>
                        <li>VIN: ${vehicle.vin}</li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Similar Vehicles -->
        <div class="similar-section" id="similar-section">
            <h2>Similar Vehicles</h2>
            <div class="similar-grid" id="similar-grid"></div>
        </div>
    `;

    // Tab switching
    detail.querySelectorAll(".tab-btn").forEach((btn) => {
        btn.addEventListener("click", () => {
            detail.querySelectorAll(".tab-btn").forEach((b) => b.classList.remove("active"));
            detail.querySelectorAll(".tab-pane").forEach((p) => p.classList.remove("active"));
            btn.classList.add("active");
            const pane = detail.querySelector(`#tab-${btn.dataset.tab}`);
            if (pane) pane.classList.add("active");
        });
    });

    // Thumbnail click → switch hero image + update counter
    detail.querySelectorAll(".detail-thumbs img").forEach((thumb, index) => {
        thumb.addEventListener("click", () => {
            detail.querySelector("#detail-hero").src = thumb.dataset.src;
            detail.querySelectorAll(".detail-thumbs img").forEach((item) => item.classList.remove("active"));
            thumb.classList.add("active");
            const counter = detail.querySelector("#image-counter");
            if (counter && images.length > 0) counter.textContent = `${index + 1} / ${images.length}`;
        });
    });

    // Auth guard on contact links
    detail.querySelectorAll(".contact-link").forEach((link) => {
        link.addEventListener("click", (event) => {
            if (!localStorage.getItem("token")) {
                event.preventDefault();
                window.location.href = "login.html";
            }
        });
    });

    // Load similar vehicles
    loadSimilarVehicles(vehicle);
}

async function loadSimilarVehicles(currentVehicle) {
    const grid = document.getElementById("similar-grid");
    if (!grid) return;
    try {
        const response = await fetch(`${API}?make=${encodeURIComponent(currentVehicle.make)}`);
        if (!response.ok) return;
        const vehicles = await response.json();
        const similar = vehicles.filter((v) => v.id !== currentVehicle.id).slice(0, 4);
        if (similar.length === 0) {
            const section = document.getElementById("similar-section");
            if (section) section.style.display = "none";
            return;
        }
        grid.innerHTML = similar.map((v) => {
            const images = parseImageUrls(v.imageUrls);
            const img = images[0] || "https://picsum.photos/id/111/400/250";
            return `
                <a class="similar-card" href="/vehicle-detail.html?id=${v.id}">
                    <img src="${img}" alt="${v.make} ${v.model}">
                    <h4>${v.make} ${v.model} ${v.manufactureYear}</h4>
                    <p class="price">$USD ${Number(v.fobPrice).toLocaleString()}</p>
                </a>`;
        }).join("");
    } catch (_err) {
        // Silently ignore — similar vehicles is optional
    }
}

/* ── WhatsApp & Email URLs ─────────────────────────────────── */
function whatsappUrl(vehicle) {
    const title = `${vehicle.manufactureYear} ${vehicle.make} ${vehicle.model}`;
    const message = `Hello Trade Japan team,\n\nI'm interested in this particular vehicle:\n${title}\n${vehicle.category} · ${vehicle.engineType}\n${Number(vehicle.mileage).toLocaleString()} km · ${vehicle.inventoryLocation}\nFOB ${formatUsd(vehicle.fobPrice)}\nVIN: ${vehicle.vin}\n\nPlease share more details.\n\n[Type your questions and personal details here]`;
    return `https://wa.me/?text=${encodeURIComponent(message)}`;
}

function emailUrl(vehicle) {
    const title = `${vehicle.manufactureYear} ${vehicle.make} ${vehicle.model}`;
    const subject = `Vehicle inquiry: ${title}`;
    const body = `Hello Trade Japan team,\n\nI'm interested in this particular vehicle:\n${title}\n${vehicle.category} · ${vehicle.engineType}\n${Number(vehicle.mileage).toLocaleString()} km · ${vehicle.inventoryLocation}\nFOB ${formatUsd(vehicle.fobPrice)}\nVIN: ${vehicle.vin}\n\nPlease share more details.\n\n[Add your questions and contact details here]`;
    return `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(TEAM_EMAIL)}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
}

/* ── Create Vehicle Form ───────────────────────────────────── */
async function submitVehicle(event) {
    event.preventDefault();
    const form = event.target;
    const msg = document.getElementById("form-msg");
    const payload = {
        vin: form.vin.value.trim(),
        make: form.make.value.trim(),
        model: form.model.value.trim(),
        manufactureYear: Number(form.manufactureYear.value),
        mileage: Number(form.mileage.value),
        engineType: form.engineType.value.trim(),
        category: form.category.value,
        inventoryLocation: form.inventoryLocation.value,
        fobPrice: Number(form.fobPrice.value),
        status: form.status.value,
        imageUrls: form.imageUrls.value.trim()
    };

    const response = await fetch("/api/admin/vehicles", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    msg.hidden = false;
    if (response.ok) {
        const saved = await response.json();
        msg.className = "msg ok full";
        msg.textContent = `Created vehicle #${saved.id} (${saved.make} ${saved.model}).`;
        form.reset();
        await loadAdminInventory();
        return;
    }

    let detail = `Request failed (${response.status})`;
    try {
        const body = await response.json();
        detail = body.message || body.detail || JSON.stringify(body);
    } catch (_ignored) {
        detail = await response.text();
    }
    msg.className = "msg err full";
    msg.textContent = detail;
}

/* ── Bootstrap ─────────────────────────────────────────────── */
document.addEventListener("DOMContentLoaded", () => {
    renderAuthenticationNavigation();

    // Catalogue filters
    const filterForm = document.getElementById("filter-form");
    if (filterForm) {
        filterForm.addEventListener("submit", (event) => {
            event.preventDefault();
            loadCatalogue(true).catch((err) => alert(err.message));
        });
        document.getElementById("reset-filters").addEventListener("click", () => {
            setTimeout(() => loadCatalogue(true).catch((err) => alert(err.message)), 0);
        });
        loadCatalogue(true).catch((err) => alert(err.message));

        // Intersection Observer for Infinite Scroll
        let scrollAnchor = document.getElementById("scroll-anchor");
        if (!scrollAnchor) {
            scrollAnchor = document.createElement("div");
            scrollAnchor.id = "scroll-anchor";
            scrollAnchor.style.height = "20px";
            document.getElementById("results").parentNode.appendChild(scrollAnchor);
        }
        const observer = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && !isLoading && !isLastPage) {
                loadCatalogue(false).catch((err) => console.error(err));
            }
        });
        observer.observe(scrollAnchor);
    }

    // Admin inventory
    const refreshInventory = document.getElementById("refresh-inventory");
    if (refreshInventory) {
        refreshInventory.addEventListener("click", () => loadAdminInventory().catch((err) => alert(err.message)));
        loadAdminInventory().catch((err) => alert(err.message));
    }

    // Admin add-vehicle form toggle
    const toggleBtn = document.getElementById("toggle-add-form");
    const formWrapper = document.getElementById("add-vehicle-wrapper");
    if (toggleBtn && formWrapper) {
        toggleBtn.addEventListener("click", () => {
            formWrapper.classList.toggle("visible");
            if (formWrapper.classList.contains("visible")) {
                formWrapper.scrollIntoView({ behavior: "smooth", block: "start" });
            }
        });
    }

    // Vehicle detail
    loadVehicleDetails().catch((err) => {
        const detail = document.getElementById("vehicle-detail");
        if (detail) detail.innerHTML = `<div style="padding:3rem;text-align:center;"><p class="msg err">${err.message}</p><a class="back-link" href="/inventory.html"><i class="bi bi-arrow-left"></i> Back to catalogue</a></div>`;
    });

    // Vehicle form
    const vehicleForm = document.getElementById("vehicle-form");
    if (vehicleForm) {
        vehicleForm.addEventListener("submit", (event) => {
            submitVehicle(event).catch((err) => {
                const msg = document.getElementById("form-msg");
                msg.hidden = false;
                msg.className = "msg err full";
                msg.textContent = err.message;
            });
        });
    }
});
