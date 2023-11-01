-- check_speed function trigger should throw exception if speed is greater than 170km/h

TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (vehicle_id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES (1, 'А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN');

TRUNCATE TABLE vehicle_movement_history CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
VALUES (1, '2023-01-01 00:00:00', 1.0, 1.0, 0),
       (1, '2023-01-01 00:02:00', 1.0, 1.0, 171 * 2);


-- check_vehicle_type should throw exception if we try to add PALLETS to OPEN vehicle
-- or BULK/TIPPER to CLOSED vehicle
TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (vehicle_id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES
    (1, 'А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN'),
    (2, 'А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'CLOSED');

TRUNCATE TABLE orders CASCADE;
ALTER SEQUENCE orders_order_id_seq RESTART WITH 1;
INSERT INTO orders (order_id, customer_id, distance, price, order_date, vehicle_id)
VALUES
    (1, 1, 800, 30000, '2023-10-25', 1),
    (2, 2, 700, 28000, '2023-10-26', 2),
    (3, 3, 500, 20000, '2023-10-27', 3);
