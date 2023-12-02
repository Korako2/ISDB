DO
$$
DECLARE
    v_vehicle_id int;
    v_vehicle_2_id int;
    v_person_id int;
    v_person_2_id int;
    v_order_id int;
    v_driver_id int;
    v_driver_2_id int;
BEGIN
-- prepare
DELETE FROM vehicle WHERE plate_number = 'А123ЕК152' OR plate_number = 'А123ЕК153';
INSERT INTO vehicle (plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES ('А123ЕК152', 'a', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN')
RETURNING id INTO v_vehicle_id;

INSERT INTO vehicle (plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
VALUES ('А123ЕК153', 'b', '2000-01-01', 1.0, 1.0, 0.0, 1000, 'OPEN')
RETURNING id INTO v_vehicle_2_id;

INSERT INTO person(first_name, last_name, middle_name, gender, date_of_birth)
VALUES ('a', 'a', 'a', 'M', '2000-01-01')
RETURNING id INTO v_person_id;

INSERT INTO person(first_name, last_name, middle_name, gender, date_of_birth)
VALUES ('b', 'b', 'b', 'M', '2000-01-01')
RETURNING id INTO v_person_2_id;

DELETE FROM driver WHERE person_id = v_person_id OR person_id = v_person_2_id OR PASSPORT = '1234567890' OR PASSPORT = '1234567891';
INSERT INTO driver (person_id, passport, bank_card_number)
VALUES (v_person_id, '1234567890', '1234567890123456')
RETURNING id INTO v_driver_id;

INSERT INTO driver (person_id, passport, bank_card_number)
VALUES (v_person_2_id, '1234567891', '1234567890123457')
RETURNING id INTO v_driver_2_id;

DELETE FROM vehicle_movement_history WHERE vehicle_id = v_vehicle_id;

BEGIN
-- check_speed function trigger should throw exception if speed is greater than 170km/h
INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
VALUES (v_vehicle_id, '2023-01-01 00:00:00', 1.0, 1.0, 0),
       (v_vehicle_id, '2023-01-01 00:02:00', 1.0, 1.0, 171 * 2);
RAISE EXCEPTION 'check_speed_trigger should be triggered';

EXCEPTION WHEN sqlstate 'T22A0' THEN
  RAISE NOTICE '%', SQLERRM;
END;

DELETE FROM vehicle_ownership WHERE vehicle_id = v_vehicle_id OR vehicle_id = v_vehicle_2_id;
BEGIN
-- check_multiple_ownership_overlap - 1 driver, 2 vehicles, time overlap
INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date, ownership_end_date)
VALUES (v_vehicle_id, v_driver_id, '2023-01-01', '2023-01-05'),
       (v_vehicle_2_id, v_driver_id, '2023-01-03', '2023-01-08');
RAISE EXCEPTION 'check_multiple_ownership_overlap should be triggered';

EXCEPTION WHEN sqlstate 'T22A0' THEN
  RAISE NOTICE '%', SQLERRM;
END;

BEGIN
-- check_single_ownership_overlap - 2 drivers, one vehicle, time overlap
INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date, ownership_end_date)
VALUES (v_vehicle_id, v_driver_id, '2023-01-01', '2023-01-05'),
       (v_vehicle_id, v_driver_2_id, '2023-01-03', '2023-01-08');
RAISE EXCEPTION 'check_multiple_ownership_overlap should be triggered';

EXCEPTION WHEN sqlstate 'T22A0' THEN
  RAISE NOTICE '%', SQLERRM;
END;
END;
$$
