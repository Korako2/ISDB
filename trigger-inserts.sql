-- check_speed function trigger should throw exception if speed is greater than 170km/h
TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES (1, 'А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN');

TRUNCATE TABLE vehicle_movement_history CASCADE;
INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
VALUES (1, '2023-01-01 00:00:00', 1.0, 1.0, 0),
       (1, '2023-01-01 00:02:00', 1.0, 1.0, 171 * 2);

-- check_vehicle_type should throw exception if we try to add PALLETS to OPEN vehicle
-- or BULK/TIPPER to CLOSED vehicle
TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES
    (1, 'А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN');

TRUNCATE TABLE orders CASCADE;
ALTER SEQUENCE orders_id_seq RESTART WITH 1;
INSERT INTO orders (id, customer_id, distance, price, order_date, vehicle_id)
VALUES
    (1, 1, 800, 30000, '2023-10-25', 1);

TRUNCATE TABLE cargo CASCADE;
INSERT INTO cargo (id, order_id, cargo_type, weight, length, width, height)
VALUES
    (1, 1, 'PALLETIZED', 1000, 1, 1, 1);


-- prepare
TRUNCATE TABLE person CASCADE;
ALTER SEQUENCE person_id_seq RESTART WITH 1;
INSERT INTO person(id, first_name, last_name, middle_name, gender, date_of_birth)
VALUES
  (1, 'a', 'a', 'a', 'M', '2000-01-01'),
  (2, 'b', 'b', 'b', 'M', '2000-01-01');

TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES
  (1, 'А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN'),
  (2, 'А123ЕК153', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN');

TRUNCATE TABLE driver CASCADE;
ALTER SEQUENCE driver_id_seq RESTART WITH 1;
INSERT INTO driver (id, person_id, passport, bank_card_number)
VALUES
  (1, 1, '1234567890', '1234567890123456'),
  (2, 2, '1234567891', '1234567890123457');

-- check_multiple_ownership_overlap - 1 driver, 2 vehicles, time overlap
TRUNCATE TABLE vehicle_ownership CASCADE;
INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date, ownership_end_date)
VALUES
    (1, 1, '2023-01-01', '2023-01-05'),
    (2, 1, '2023-01-03', '2023-01-08');


-- check_single_ownership_overlap - 2 drivers, one vehicle, time overlap
TRUNCATE TABLE vehicle_ownership CASCADE;
INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date, ownership_end_date)
VALUES
    (1, 1, '2023-01-01', '2023-01-05'),
    (1, 2, '2023-01-03', '2023-01-08');
