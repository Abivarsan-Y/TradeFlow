INSERT IGNORE INTO users (user_id, first_name, email, role) VALUES
(1, 'Admin', 'admin@tradejapan.com', 'ADMIN'),
(2, 'Customer', 'customer@example.com', 'CUSTOMER');

INSERT IGNORE INTO makes (make_id, name) VALUES
(1, 'Toyota'), (2, 'Nissan'), (3, 'Honda');

INSERT IGNORE INTO models (model_id, make_id, name) VALUES
(1, 1, 'Corolla Axio'), (2, 2, 'X-Trail'), (3, 3, 'Stepwgn Spada');

INSERT IGNORE INTO features (feature_id, name) VALUES
(1, 'Navigation'), (2, 'Alloy Wheels'), (3, 'Sunroof'), (4, 'Leather Seats');

INSERT IGNORE INTO vehicles (vehicle_id, model_id, year, price_usd, ref_no, body_type, fuel_type, steering, transmission, drive_type, chassis_no, model_code, grade, engine_model, odometer_km, displacement_cc, doors, passengers, dimension, size, exterior_color, location, description, is_new_arrival, is_active, status, created_at) VALUES
(1, 1, 2018, 9800.00, 'TJ00001', 'Sedan', 'Petrol', 'Right', 'CVT', '2WD', 'NRE161-123456', 'DBA-NRE161', 'X', '1NR-FE', 42000, 1496, 4, 5, '4400x1695x1460', 'Standard', 'White', 'Japan', 'Great condition family sedan.', TRUE, TRUE, 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 2, 2019, 15450.00, 'TJ00002', 'SUV', 'Hybrid', 'Right', 'CVT', '4WD', 'HNT32-123456', 'DAA-HNT32', '20X', 'MR20DD-RM31', 61000, 1997, 5, 5, '4690x1820x1730', 'Standard', 'Black', 'Japan', 'Spacious hybrid SUV.', FALSE, TRUE, 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 3, 2017, 11200.00, 'TJ00003', 'Van', 'Petrol', 'Right', 'CVT', '2WD', 'RP3-123456', 'DBA-RP3', 'Spada', 'L15B', 88000, 1496, 5, 7, '4760x1695x1840', 'Standard', 'Silver', 'Japan', 'Family van with lots of space.', FALSE, TRUE, 'RESERVED', DATE_SUB(NOW(), INTERVAL 45 DAY));

INSERT IGNORE INTO vehicle_images (image_id, vehicle_id, image_url, is_main, sort_order) VALUES
(1, 1, 'https://res.cloudinary.com/demo/image/upload/w_800/sample.jpg', TRUE, 1),
(2, 1, 'https://picsum.photos/id/111/800/500', FALSE, 2),
(3, 2, 'https://picsum.photos/id/1071/800/500', TRUE, 1),
(4, 2, 'https://picsum.photos/id/1072/800/500', FALSE, 2),
(5, 3, 'https://picsum.photos/id/655/800/500', TRUE, 1);

INSERT IGNORE INTO vehicle_features (vehicle_id, feature_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 3), (2, 4),
(3, 1);

INSERT IGNORE INTO saved_vehicles (user_id, vehicle_id) VALUES
(2, 1), (2, 2);

INSERT IGNORE INTO app_initialization (name) VALUES ('initial_vehicles');
