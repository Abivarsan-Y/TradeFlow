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
(1, 1, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.carjunction.com%2Fcar_images2%2F94990_106925%2F106925aa-(1).jpg&f=1&nofb=1&ipt=f49e0a682d692a1bcf2110b5cab97f324fbcdfbf70aab07ce500ccb39449f469&ipo=images', TRUE, 1),
(2, 1, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fautovanjp.com%2Fwp-content%2Fuploads%2F2023%2F01%2FTOYOTA-COROLLA-AXIO-9.jpeg&f=1&nofb=1&ipt=460dae0f5f89799aa5443e4cf0146c64ed054ffaf46d35d508067f880cb771aa&ipo=images', FALSE, 2),
(3, 2, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fs1.cdn.autoevolution.com%2Fimages%2Fnews%2Fgallery%2F2019-nissan-x-trail-gets-17-liter-diesel-with-150-hp-13-turbo-with-160-hp_6.jpg&f=1&nofb=1&ipt=00a467b2a2cee306a39a958e45acc649b68f921ed317c428bf69ca98b98c66d9&ipo=images', TRUE, 1),
(4, 2, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fcdn.motor1.com%2Fimages%2Fmgl%2Fz21Wb%2Fs1%2Fnissan-x-trail-2019-neue-motoren.jpg&f=1&nofb=1&ipt=a7767daa5aabb725ee3d15dc4886b5a55ee992183e7bfd72f7a8575387d8fc14&ipo=images', FALSE, 2),
(5, 3, 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Favatars.mds.yandex.net%2Fget-autoru-vos%2F4946962%2F6777edce082bb5734befa67c28c346a7%2F1200x900&f=1&nofb=1&ipt=fc225c56ef35423033ffe52accb8d2dd0448b7b6d3a08ebbe1b0e620124c1e8e&ipo=images', TRUE, 1);

INSERT IGNORE INTO vehicle_features (vehicle_id, feature_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 3), (2, 4),
(3, 1);

INSERT IGNORE INTO saved_vehicles (user_id, vehicle_id) VALUES
(2, 1), (2, 2);

INSERT IGNORE INTO app_initialization (name) VALUES ('initial_vehicles');
