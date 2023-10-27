TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (vehicle_id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES (1, 'А123ЕК152', 'BMW', '2019-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN'),
       (2, 'А111АА52', 'Mercedes', '2018-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN'),
       (3, 'О001ОО152', 'Audi', '2017-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN');

TRUNCATE TABLE vehicle_movement_history CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
VALUES (1, '2019-01-01 00:00:00', 1.0, 1.0, 1000),
       (2, '2019-01-01 00:00:01', 1.0, 1.0, 1400),
       (1, '2019-01-01 00:00:03', 1.0, 1.0, 1100);

TRUNCATE TABLE address CASCADE;
ALTER SEQUENCE address_address_id_seq RESTART WITH 1;
INSERT INTO address (address_id, country, city, street, building, corpus)
VALUES (1, 'Россия', 'Санкт-Петербург', 'Кронверский проспект', '49', NULL),
       (2, 'Россия', 'Москва', 'Новая Басманная', 4, 1),
       (3, 'Беларусь', 'Минск', 'Кирова', 8, 7),
       (4, 'Беларусь', 'Солигорск', 'Козлова', 35, NULL);


TRUNCATE TABLE storage_point CASCADE;
INSERT INTO storage_point(address_id, longitude, latitude)
VALUES (1, 59.956363, 30.310029),
       (2, 55.769048, 37.653765),
       (3, 53.895058, 27.561382),
       (4, 52.784080, 27.543973);


TRUNCATE TABLE person CASCADE;
ALTER SEQUENCE person_person_id_seq RESTART WITH 1;
INSERT INTO person (person_id, first_name, last_name, middle_name, gender, date_of_birth)
VALUES
  (1, 'Иван', 'Иванов', 'Иванович', 'M', '1990-10-25'),
  (2, 'Мария', 'Петрова', 'Александровна', 'F', '1980-11-23'),
  (3, 'Алексей', 'Сидоров', NULL, 'M', '1985-01-01'),
  (4, 'Екатерина', 'Смирнова', 'Андреевна', 'F', '2000-01-01'),
  (5, 'Иван', 'Варюхин', 'Андреевич', 'M', '2000-01-12');

TRUNCATE TABLE driver CASCADE;
ALTER SEQUENCE driver_driver_id_seq RESTART WITH 1;
INSERT INTO driver (driver_id, person_id, passport, bank_card_number)
VALUES
  (1, 4, '1234567890', '1234 5678 9012 3456'),
  (2, 5, '0987654321', '9876 5432 1098 7654');

TRUNCATE TABLE customer CASCADE;
ALTER SEQUENCE customer_customer_id_seq RESTART WITH 1;
INSERT INTO customer (customer_id, person_id, organization)
VALUES
  (1, 1, 'ООО Рога и Копыта'),
  (2, 2, 'ИП Иванов И.И.'),
  (3, 3, 'ЗАО Петров и Партнеры');

TRUNCATE TABLE orders CASCADE;
ALTER SEQUENCE orders_order_id_seq RESTART WITH 1;
INSERT INTO orders (order_id, customer_id, distance, price, order_date, vehicle_id)
VALUES
  (1, 1, 800, 30000, '2023-10-25', 1),
  (2, 2, 700, 28000, '2023-10-26', 2),
  (3, 3, 500, 20000, '2023-10-27', 3);


TRUNCATE TABLE cargo CASCADE;
ALTER SEQUENCE cargo_cargo_id_seq RESTART WITH 1;
INSERT INTO cargo (cargo_id, weight, width, height, length, order_id, cargo_type)
VALUES
  (1,900.0, 1.0, 1.9, 4.0, 1, 'BULK'),
  (2,800.0, 1.9, 1.7, 3.5, 2, 'TIPPER'),
  (3,700.0, 1.8, 1.8, 3.5, 3, 'PALLETIZED');

TRUNCATE TABLE loading_unloading_agreement CASCADE;
INSERT INTO loading_unloading_agreement (order_id, driver_id, departure_point, delivery_point, sender_id, receiver_id, unloading_time, loading_time)
VALUES
  (1, 1, 1, 2, 1, 2, '08:00', '16:00'),
  (2, 2, 2, 1, 3, 4, '10:00', '18:00');

TRUNCATE TABLE order_statuses CASCADE;
INSERT INTO order_statuses (order_id, time, status)
VALUES
  (1, '2023-01-01 00:00:01', 'ACCEPTED'),
  (2, '2023-01-01 01:01:02', 'ACCEPTED'),
  (1, '2023-01-01 00:00:02', 'IN PROGRESS'),
  (1, '2023-01-01 01:01:01', 'ARRIVED AT LOADING LOCATION'),
  (1, '2023-01-01 01:01:02', 'LOADING'),
  (1, '2023-01-01 01:01:03', 'ARRIVED AT UNLOADING LOCATION'),
  (1, '2023-01-01 01:01:04', 'ON THE WAY'),
  (1, '2023-01-01 01:01:05', 'COMPLETED');

TRUNCATE TABLE tariff_rate CASCADE;
INSERT INTO tariff_rate (driver_id, daily_rate, rate_per_km)
VALUES
  (1, 1000, 10),
  (2, 900, 8);

TRUNCATE TABLE driver_license CASCADE;
INSERT INTO driver_license (driver_id, issue_date, expiration_date, license_number)
VALUES
  (1, '2010-01-01', '2023-01-01', 1234542),
  (2, '2012-05-15', '2024-05-15', 5432112);

TRUNCATE TABLE vehicle_ownership CASCADE;
INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date, ownership_end_date)
VALUES
  (1, 1, '2023-01-01', '2023-12-31'),
  (2, 2, '2023-01-01', '2023-12-31');

TRUNCATE TABLE fuel_cards_for_drivers  CASCADE;
INSERT INTO fuel_cards_for_drivers (driver_id, fuel_card_number, fuel_station_name)
VALUES
  (1, '1234567890123456', 'Газпром'),
  (2, '9876543210987654', 'Лукойл');

TRUNCATE TABLE fuel_expenses CASCADE;
INSERT INTO fuel_expenses (fuel_card_number, date, amount)
VALUES
  ('1234567890123456', '2023-10-25', 120.50),
  ('9876543210987654', '2023-10-26', 110.20);