
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
