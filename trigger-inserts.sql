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
