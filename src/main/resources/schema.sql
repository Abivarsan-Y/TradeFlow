-- ============================================================
-- TradeJapan
--
-- ALL tables are created here. Hibernate is NOT allowed to create or
-- change tables (spring.jpa.hibernate.ddl-auto=none).
--   Section 1: Inventory   (Abivarsan)
--   Section 2: CMS / Auth  (Rahman)
--   Section 3: Inquiries & Purchases (Sarthani)
--   Section 4: Wishlist
--   Section 5: Contact Us & Testimonials (Aazir)
-- Safe to run many times (CREATE TABLE IF NOT EXISTS).
-- Seed data is in data.sql.
-- ============================================================

-- ------------------------------------------------------------
-- SECTION 1: INVENTORY (Abivarsan)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS makes (
    make_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS models (
    model_id INT AUTO_INCREMENT PRIMARY KEY,
    make_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    FOREIGN KEY (make_id) REFERENCES makes(make_id)
);

CREATE TABLE IF NOT EXISTS features (
    feature_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    model_id INT NOT NULL,
    year INT NOT NULL,
    price_usd DECIMAL(10,2) NOT NULL,
    ref_no VARCHAR(30) UNIQUE NOT NULL,
    body_type VARCHAR(50),
    fuel_type VARCHAR(50),
    steering VARCHAR(50),
    transmission VARCHAR(50),
    drive_type VARCHAR(50),
    chassis_no VARCHAR(50),
    model_code VARCHAR(50),
    grade VARCHAR(50),
    engine_model VARCHAR(50),
    odometer_km INT NOT NULL,
    displacement_cc INT,
    doors INT,
    passengers INT,
    dimension VARCHAR(50),
    size VARCHAR(50),
    exterior_color VARCHAR(30),
    location VARCHAR(50),
    description TEXT,
    is_new_arrival BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    vehicle_condition VARCHAR(10) NOT NULL DEFAULT 'USED',   -- NEW / USED (badge on the vehicle page)
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES models(model_id)
);

CREATE TABLE IF NOT EXISTS vehicle_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    image_url TEXT NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

ALTER TABLE vehicle_images MODIFY COLUMN image_url TEXT NOT NULL;

CREATE TABLE IF NOT EXISTS vehicle_features (
    vehicle_id INT NOT NULL,
    feature_id INT NOT NULL,
    PRIMARY KEY (vehicle_id, feature_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES features(feature_id) ON DELETE CASCADE
    );
-- ------------------------------------------------------------
-- SECTION 2: part1 (Rahman)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NULL,                     -- NULL for Google sign-in users
    first_name        VARCHAR(255),
    last_name         VARCHAR(255),
    whatsapp_no       VARCHAR(255),
    role              VARCHAR(30)  NOT NULL,                 -- ROLE_CUSTOMER / ROLE_ADMIN / ROLE_SUPER_ADMIN
    status            VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    auth_provider     VARCHAR(30)  NOT NULL DEFAULT 'LOCAL',
    google_id         VARCHAR(255) UNIQUE,
    accepted_terms_at DATETIME NULL,
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    BIGINT NOT NULL UNIQUE,                       -- one active token per user
    expires_at DATETIME NOT NULL,
    used_at    DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS banners (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    size        VARCHAR(30) NOT NULL,
    media_type  VARCHAR(30) NOT NULL,
    image_url   VARCHAR(1000),
    link_url    VARCHAR(1000),
    link_type   VARCHAR(255),
    button_text VARCHAR(255),
    title       VARCHAR(255),
    subtitle    VARCHAR(255),
    section_num INT,
    sort_order  INT DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS content (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    page_name    VARCHAR(255),
    title        VARCHAR(255),
    html_content TEXT,
    updated_by   BIGINT,
    updated_at   DATETIME
);

CREATE TABLE IF NOT EXISTS faq_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    question      VARCHAR(500),
    answer        TEXT,
    display_order INT,
    active        BOOLEAN NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------
-- SECTION 4: part4 - Customer Engagement & Reporting
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS contact_messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handled_by BIGINT NULL,
    handled_at DATETIME NULL,

    CONSTRAINT fk_contact_handled_by
        FOREIGN KEY (handled_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    INDEX idx_contact_status (status),
    INDEX idx_contact_handled_by (handled_by)
);

CREATE TABLE IF NOT EXISTS testimonials (
    testimonial_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    rating INT NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,

    CONSTRAINT fk_testimonial_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_testimonial_reviewed_by
        FOREIGN KEY (reviewed_by)
        REFERENCES users(id)
        ON DELETE SET NULL,

    INDEX idx_testimonial_status (status),
    INDEX idx_testimonial_user (user_id),
    INDEX idx_testimonial_reviewed_by (reviewed_by),

    CONSTRAINT chk_testimonial_rating
        CHECK (rating BETWEEN 1 AND 5)
);
