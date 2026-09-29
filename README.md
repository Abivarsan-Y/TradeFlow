# Trade Japan — Member 3: Inquiries & Purchases

This folder contains **Member 3 only**, based on the final EER section **PART 3 - Inquiries & Purchases**.

## Member 3 database entities

### Inquiry
- inquiry_id
- user_id
- vehicle_id
- subject
- message
- destination_country
- status
- created_at

### Inquiry Reply
- reply_id
- inquiry_id
- user_id
- message
- created_at

### Purchase
- purchase_id
- user_id (buyer)
- vehicle_id
- processed_by (admin user)
- inquiry_id (optional and unique: 0..1 inquiry -> 0..1 purchase)
- invoice_no
- invoice_file_url
- total_price
- advance_paid
- status
- notes
- created_at

The Purchase entity intentionally does **not** include shipping/logistics fields because those belong to Member 4.

## Backend files

```text
src/main/java/com/tradejapan/member3/
├── TradeJapanMember3Application.java
├── entity/
│   ├── Inquiry.java
│   ├── InquiryReply.java
│   ├── InquiryStatus.java
│   ├── Purchase.java
│   └── PurchaseStatus.java
├── repository/
│   ├── InquiryRepository.java
│   ├── InquiryReplyRepository.java
│   └── PurchaseRepository.java
├── service/
│   ├── InquiryService.java
│   ├── PurchaseService.java
│   └── InvoiceStorageService.java
├── controller/
│   ├── InquiryController.java
│   ├── AdminInquiryController.java
│   ├── PurchaseController.java
│   └── AdminPurchaseController.java
├── dto/
│   ├── CreateInquiryRequest.java
│   ├── CreateReplyRequest.java
│   ├── UpdateInquiryStatusRequest.java
│   ├── CreatePurchaseRequest.java
│   ├── UpdateAdvancePaymentRequest.java
│   └── UpdatePurchaseStatusRequest.java
└── exception/
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

## Frontend files

```text
src/main/resources/static/
├── index.html
├── inquiry.html
├── my-inquiries.html
├── my-purchases.html
├── admin-inquiries.html
├── admin-purchases.html
├── css/
│   └── styles.css
└── js/
    ├── inquiry.js
    ├── my-inquiries.js
    ├── admin-inquiries.js
    ├── my-purchases.js
    └── admin-purchases.js
```

## Database SQL

```text
src/main/resources/database/member3-mysql.sql
```

Run that SQL only after the team's `users` and `vehicles` tables exist.

## Purchase workflow

```text
PENDING
   ↓
ADVANCE_PAID
   ↓
FULLY_PAID
   ↓
DELIVERED
```

An inquiry can optionally lead to one purchase. Once an inquiry is linked to a purchase, the admin inquiry delete endpoint refuses to delete it so purchase history is not broken.

## Main purchase APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/admin/purchases` | Admin creates purchase |
| GET | `/api/admin/purchases` | Admin lists purchases |
| GET | `/api/admin/purchases?status=PENDING` | Filter purchase status |
| PATCH | `/api/admin/purchases/{id}/advance` | Record advance amount |
| PATCH | `/api/admin/purchases/{id}/status` | Move purchase to next status |
| POST | `/api/admin/purchases/{id}/invoice` | Upload invoice PDF |
| GET | `/api/purchases/user/{userId}` | Customer purchase history demo |
| GET | `/api/purchases/{id}` | Purchase details |
| GET | `/api/purchases/{id}/invoice` | Download invoice PDF |

## Run in VS Code / IntelliJ

Open the folder that contains `pom.xml`, then run the Spring Boot main class:

```text
TradeJapanMember3Application.java
```

Open the website through Spring Boot (not the VS Code/IntelliJ static preview server):

```text
http://localhost:8080/
http://localhost:8080/inquiry.html
http://localhost:8080/my-inquiries.html
http://localhost:8080/my-purchases.html
http://localhost:8080/admin-inquiries.html
http://localhost:8080/admin-purchases.html
```

Default local development uses H2. MySQL configuration is in `application-mysql.properties`.

Invoice PDFs are stored in `private-invoices/`, which is ignored by Git. Do not commit real customer invoices, `.env`, `data/`, or `target/`.

## Group integration

- Member 1 owns the real Vehicle entity/table.
- Member 2 owns User/login/security.
- Member 3 owns Inquiry, InquiryReply and Purchase.
- Member 4 owns export compliance and logistics/shipping tracking.

When the group merges the project, replace the temporary numeric user/vehicle demo handling with the group's real authenticated User and Vehicle integration. Do not create duplicate JPA mappings for the same shared tables.
