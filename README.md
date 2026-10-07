# Trade Japan — Combined Project (Members 1, 2, 3 and 4)

UI = Rahman's original UI (navbar, footer, login/register, profile, FAQ, content, admin dashboard).
Vehicle pages, Inquiries & Purchases and Contact Us / Testimonials / Reports were added in the same design.

| Member | Module | Package |
|---|---|---|
| 1 Abivarsan | Vehicle inventory | `com.tradejapan.inventory` |
| 2 Rahman | TradeJapan | part1 | `com.tradejapan.part1` |
| 3 Sarthani | Inquiries & Purchases | `com.tradejapan.part3` |
| 4 Aazir | Contact Us, Testimonials, Dashboard & Reports | `com.tradejapan.part4` |

## Run
1. Start MySQL (XAMPP or MySQL Server) on port 3306.
2. Set `spring.datasource.password` in `src/main/resources/application.properties` if your root user has one.
3. Run `TradeJapanApplication.java`  ->  http://localhost:8081  (change `server.port` if needed)

The database `tradejapan` is created automatically, and **all tables come from `schema.sql`** (seed data from `data.sql`).
Hibernate is NOT allowed to create or change tables (`spring.jpa.hibernate.ddl-auto=none`).

**If you add a field to an entity, add the column to `schema.sql` too, then run `DROP DATABASE tradejapan;` and start again.**
The super admin account and the 28 vehicle features are created by Java code at startup.

## Logins (same as the original )
- Super Admin: `superadmin@example.com` / `superadmin123`  -> http://localhost:8081/pages/login.html (or /pages/admin/login.html)

## Pages
- `/` Home (banners from , live New Arrivals)
- `/pages/inventory.html` Vehicle catalogue (filters, load more)
- `/pages/vehicle-detail.html?id=1`
- `/pages/contact.html` Contact Us, `/pages/submit-testimonial.html` testimonial form (login needed); approved testimonials show on the home page
- `/pages/admin/dashboard.html` -> Dashboard (live numbers), Vehicles, Messages (Inquiries + Contact Messages tabs), Purchases, Testimonials, Content, FAQ, Admins, Reports

## API (unchanged from original + inventory)
- Public: `GET /api/vehicles`, `/api/vehicles/{id}`, `/api/vehicles/new-arrivals`
- Admin / Super Admin: `/api/admin/vehicles/**`
- Super Admin only: `/api/admin/users/**`, `/api/admin/reports/**`

## Inquiries & Purchases (Member 3)
Customer flow: Vehicle page -> **Send Inquiry** -> My Account -> **Message** tab (conversation with replies) -> **Purchase History** tab (progress, balance, invoice PDF).
Admin flow: Dashboard -> **Messages** (reply, change status, create purchase from an inquiry) -> **Purchases** (advance payment, invoice upload, move status).

Purchase status order: PENDING -> ADVANCE_PAID -> FULLY_PAID -> DELIVERED.

Customer endpoints (JWT login required, always scoped to the logged-in user — no userId in URLs):
- `POST /api/inquiries`, `GET /api/inquiries/me`, `GET /api/inquiries/{id}`, `GET /api/inquiries/{id}/replies`
- `GET /api/purchases/me`, `GET /api/purchases/{id}`, `GET /api/purchases/{id}/invoice`

Admin / Super Admin endpoints:
- `GET /api/admin/inquiries[?status=]`, `PATCH /api/admin/inquiries/{id}/status`, `POST /api/admin/inquiries/{id}/replies`, `DELETE /api/admin/inquiries/{id}`
- `POST /api/admin/purchases`, `GET /api/admin/purchases[?status=]`, `PATCH .../{id}/advance`, `PATCH .../{id}/status`, `POST .../{id}/invoice`

Invoice PDFs are stored in `private-invoices/` (git-ignored).

## Vehicle page & Edit Vehicle form
- Vehicle page: gallery, key facts, **Inquire Now**, **WhatsApp**, tabs **Overview / Specifications (18) / Features (28 ON-OFF)**.
- WhatsApp number: `TJ_WHATSAPP_NUMBER` at the top of the detail-page section in `static/js/vehicles.js` (digits only, with country code).
- Admin -> Vehicles -> Add/Edit uses the same three tabs. Every field on Overview and all 18 Specification fields are required (checked in the browser and again on the server).
- The 28 features are defined once in `inventory/config/FeatureCatalog.java` and created automatically at startup by `FeatureSeeder.java`.
- Changing a vehicle's (or inquiry's) status from the table asks for confirmation first.
- `PATCH /api/admin/vehicles/{id}/status`, `GET /api/features` (public), `PUT /api/admin/vehicles/{id}` now takes the full payload incl. `featureIds`.

## Contact Us, Testimonials, Dashboard & Reports (Member 4)
- Public: `POST /api/contact-messages` (Contact Us form), `GET /api/testimonials/approved` (home page).
- Customer (login): `POST /api/testimonials` (starts PENDING, max 3 waiting), `GET /api/testimonials/me`.
- Admin / Super Admin: `GET /api/admin/contact-messages`, `PATCH .../{id}/status` (NEW / READ / RESOLVED), `DELETE .../{id}`;
  `GET /api/admin/testimonials`, `PATCH .../{id}/approve|reject`, `DELETE .../{id}`; `GET /api/admin/dashboard-stats`.
- Super Admin only: `GET /api/admin/reports/sales-summary`, `GET /api/admin/reports/admin-activity`.
- Inquiry chat: customers and admins can both send any number of messages; the admin table shows how many customer messages are unread. Messages and Purchases pages have search / filter bars (customer, make, destination, status, date range, and an Admin filter for the Super Admin).
