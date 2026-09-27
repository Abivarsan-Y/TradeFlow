-- TradeFlow: Vehicle Inventory & Catalogue

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50),
    email VARCHAR(100) UNIQUE,
    role VARCHAR(20) DEFAULT 'CUSTOMER'
);

CREATE TABLE makes (
    make_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE models (
    model_id INT AUTO_INCREMENT PRIMARY KEY,
    make_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    FOREIGN KEY (make_id) REFERENCES makes(make_id)
);

CREATE TABLE features (
    feature_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE vehicles (
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
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (model_id) REFERENCES models(model_id)
);

CREATE TABLE vehicle_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

CREATE TABLE vehicle_features (
    vehicle_id INT NOT NULL,
    feature_id INT NOT NULL,
    PRIMARY KEY (vehicle_id, feature_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (feature_id) REFERENCES features(feature_id) ON DELETE CASCADE
);

CREATE TABLE saved_vehicles (
    user_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, vehicle_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
);

CREATE TABLE app_initialization (
    name VARCHAR(80) NOT NULL PRIMARY KEY
);
