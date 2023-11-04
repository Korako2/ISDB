TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES ('А123ЕК152', 'BMW', '2019-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN'),
       ('А111АА52', 'Mercedes', '2018-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN'),
       ('О001ОО152', 'Audi', '2017-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN');

TRUNCATE TABLE vehicle_movement_history CASCADE;
INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
VALUES (1, '2023-01-01 00:00:00', 1.0, 1.0, 1000),
       (2, '2023-01-01 00:10:01', 2.0, 2.0, 1010),
       (1, '2023-01-01 00:20:03', 3.0, 3.0, 1020);

TRUNCATE TABLE address CASCADE;
ALTER SEQUENCE address_address_id_seq RESTART WITH 1;
INSERT INTO address (country, city, street, building, corpus)
VALUES ('Россия', 'Санкт-Петербург', 'Кронверский проспект', '49', NULL),
       ('Россия', 'Москва', 'Новая Басманная', 4, 1),
       ('Беларусь', 'Минск', 'Кирова', 8, 7),
       ('Беларусь', 'Солигорск', 'Козлова', 35, NULL);

TRUNCATE TABLE storage_point CASCADE;
INSERT INTO storage_point(address_id, latitude, longitude)
VALUES (1, 59.956363, 30.310029),
       (2, 55.769048, 37.653765),
       (3, 53.895058, 27.561382),
       (4, 52.784080, 27.543973);

TRUNCATE TABLE person CASCADE;
ALTER SEQUENCE person_person_id_seq RESTART WITH 1;
INSERT INTO person (first_name, last_name, middle_name, gender, date_of_birth)
VALUES
  ('Иван', 'Иванов', 'Иванович', 'M', '1990-10-25'),
  ('Мария', 'Петрова', 'Александровна', 'F', '1980-11-23'),
  ('Алексей', 'Сидоров', NULL, 'M', '1985-01-01'),
  ('Екатерина', 'Смирнова', 'Андреевна', 'F', '2000-01-01'),
  ('Иван', 'Варюхин', 'Андреевич', 'M', '2000-01-12');

TRUNCATE TABLE driver CASCADE;
ALTER SEQUENCE driver_driver_id_seq RESTART WITH 1;
INSERT INTO driver (person_id, passport, bank_card_number)
VALUES
  (4, '1234567890', '1234 5678 9012 3456'),
  (5, '0987654321', '9876 5432 1098 7654');
--todo вернуть id и сделать его человеком

TRUNCATE TABLE customer CASCADE;
ALTER SEQUENCE customer_customer_id_seq RESTART WITH 1;
INSERT INTO customer (person_id, organization)
VALUES
  (1, 'ООО Рога и Копыта'),
  (2, 'ИП Иванов И.И.'),
  (3, 'ЗАО Петров и Партнеры');
--todo вернуть id и сделать его человеком

TRUNCATE TABLE orders CASCADE;
ALTER SEQUENCE orders_order_id_seq RESTART WITH 1;
INSERT INTO orders (customer_id, distance, price, order_date, vehicle_id)
VALUES
  (1, 800, 30000, '2023-10-25', 1),
  (2, 700, 28000, '2023-10-26', 2),
  (3, 500, 20000, '2023-10-27', 3);


TRUNCATE TABLE cargo CASCADE;
ALTER SEQUENCE cargo_cargo_id_seq RESTART WITH 1;
INSERT INTO cargo (weight, width, height, length, order_id, cargo_type)
VALUES
  (900.0, 1.0, 1.9, 4.0, 1, 'BULK'),
  (800.0, 1.9, 1.7, 3.5, 2, 'TIPPER'),
  (700.0, 1.8, 1.8, 3.5, 3, 'PALLETIZED');

TRUNCATE TABLE loading_unloading_agreement CASCADE;
INSERT INTO loading_unloading_agreement (order_id, driver_id, departure_point, delivery_point, sender_id, receiver_id, unloading_time, loading_time)
VALUES
  (1, 1, 1, 2, 1, 2, '08:00', '16:00'),
  (2, 2, 2, 1, 3, 4, '10:00', '18:00');

TRUNCATE TABLE tariff_rate CASCADE;
INSERT INTO tariff_rate (driver_id, daily_rate, rate_per_km)
VALUES
  (1, 1000, 10),
  (2, 900, 8);

TRUNCATE TABLE driver_license CASCADE;
INSERT INTO driver_license (driver_id, issue_date, expiration_date, license_number)
VALUES
  (1, '1990-01-01', '2040-01-01', 1234542),
  (2, '1980-05-15', '2042-05-15', 5432112);

TRUNCATE TABLE vehicle_ownership CASCADE;
INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date, ownership_end_date)
VALUES
  (1, 1, '1990-01-01', null),
  (2, 2, '1990-01-01', null);

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

TRUNCATE TABLE driver_status_history CASCADE;
TRUNCATE TABLE order_statuses CASCADE;
INSERT INTO driver_status_history (driver_id, date, status)
VALUES
  (1, '2023-10-25 00:00:00', 'ACCEPTED ORDER'),
  (1, '2023-10-25 00:00:02', 'ARRIVED AT LOADING LOCATION'),
  (1, '2023-10-25 00:00:03', 'LOADING'),
  (1, '2023-10-25 00:00:05', 'EN ROUTE'),
  (1, '2023-10-25 00:00:06', 'ARRIVED AT UNLOADING LOCATION'),
  (1, '2023-10-25 00:00:07', 'UNLOADING'),
  (1, '2023-10-25 00:00:08', 'COMPLETED ORDER'),
  (1, '2023-10-25 00:00:01', 'OFF DUTY'),
  (2, '2023-10-25 00:00:10', 'ACCEPTED ORDER'),
  (2, '2023-10-25 00:00:11', 'ARRIVED AT LOADING LOCATION'),
  (2, '2023-10-25 00:00:12', 'LOADING'),
  (2, '2023-10-25 00:00:14', 'EN ROUTE'),
  (2, '2023-10-25 00:00:15', 'ARRIVED AT UNLOADING LOCATION'),
  (2, '2023-10-25 00:00:16', 'UNLOADING'),
  (2, '2023-10-25 00:00:17', 'COMPLETED ORDER');
