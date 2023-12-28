-- demo: select * from person join customer on person.id = customer.person_id ORDER BY person_id DESC LIMIT 1;

DO
$do$
DECLARE
    cargo_length int := 1;
    v_driver_id int;
    v_vehicle_id int;
    v_order_id int;
    v_customer_id int;
    ve_plate_number varchar;
    ve_body_type varchar;
BEGIN
SELECT add_new_customer(
  v_first_name => 'Ivanov',
  v_last_name => 'Ivanov',
  v_gender => 'M',
  v_date_of_birth => '1970-01-01',
  v_middle_name => 'Ivanovich'
) INTO v_customer_id;

RAISE NOTICE 'Added new customer with id: %', v_customer_id;

DELETE FROM driver WHERE passport = '1234567890';
SELECT add_driver(
  v_first_name => 'Oleg',
  v_last_name => 'Olegovhich',
  v_middle_name => 'Olegov',
  v_gender => 'M',
  v_date_of_birth => '1990-01-01',
  v_passport => '1234567890',
  v_bank_card_number => '1234567890123456'
) INTO v_driver_id;

RAISE NOTICE 'Driver added with id: %', v_driver_id;

CALL add_driver_info(v_driver_id,
  v_daily_rate => 1000,
  v_rate_per_km => 10,
  v_issue_date => '2020-01-01',
  v_expiration_date => '2040-01-01',
  v_license_number => 1234567890,
  v_fuel_card => '1234567890123456',
  v_fuel_station_name => 'Gazprom'
);

SELECT closest_vehicle_id FROM find_suitable_vehicle(
  v_length => cargo_length,
  v_width => 1,
  v_height => 1,
  v_cargo_type => 'BULK',
  v_weight => 100,
  cargo_latitude => 47.5,
  cargo_longitude => 23.54
) INTO v_vehicle_id;


SELECT plate_number, body_type INTO ve_plate_number, ve_body_type FROM vehicle WHERE id = v_vehicle_id;
RAISE NOTICE 'Suitable vehicle "%" of type "%" found: %', ve_plate_number, ve_body_type, v_vehicle_id;

SELECT add_order(
  var_customer_id => v_customer_id,
  distance => 100,
  var_vehicle_id => v_vehicle_id,
  v_weight => 100,
  v_width => 1,
  v_height => 1,
  v_length => cargo_length,
  v_cargo_type => 'BULK',
  v_date => NOW()::timestamp
  ) INTO v_order_id;

RAISE NOTICE 'New order_id: %', v_order_id;

END
$do$;
