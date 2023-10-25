TRUNCATE TABLE vehicle CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle (vehicle_id, plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES (1, 'А123ЕК152', 'BMW', '2019-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN'),
       (2, 'А111АА52', 'Mercedes', '2018-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN'),
       (3, 'О001ОО152', 'Audi', '2017-01-01', 4.5, 2.0, 2.0, 1000, 'OPEN');

-- insert 2 values into vehicle history table
TRUNCATE TABLE vehicle_movement_history CASCADE;
ALTER SEQUENCE vehicle_vehicle_id_seq RESTART WITH 1;
INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
VALUES (1, '2019-01-01 00:00:00', 1.0, 1.0, 1000),
       (2, '2019-01-01 00:00:01', 1.0, 1.0, 1400),
       (1, '2019-01-01 00:00:03', 1.0, 1.0, 1100);

TRUNCATE TABLE address CASCADE;
ALTER SEQUENCE address_id_seq RESTART WITH 1;
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
