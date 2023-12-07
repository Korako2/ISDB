DROP TABLE IF EXISTS
    vehicle_ownership,
    loading_unloading_agreement,
    storage_point,
    address,
    cargo,
    order_statuses,
    orders,
    vehicle_movement_history,
    vehicle,
    driver_license,
    driver_status_history,
    tariff_rate,
    fuel_cards_for_drivers,
    fuel_expenses,
    driver,
    customer,
    contact_info,
    person
    CASCADE;

DROP TYPE IF EXISTS
    driver_status,
    body_type,
    cargo_type,
    order_status,
    contact_info_type
    CASCADE;

DROP FUNCTION IF EXISTS
  add_order,
  add_new_customer,
  check_speed,
  check_vehicle_type,
  check_fuel_expenses,
  check_cargo_size,
  check_country_match,
  check_order_status_sequence,
  check_order_status_time,
  update_order_status,
  add_customer,
  add_driver,
  add_vehicle,
  find_car_to_fit_size,
  find_suitable_vehicle,
  check_multiple_ownership_overlap,
  check_single_ownership_overlap
  CASCADE;

DROP PROCEDURE IF EXISTS
    add_driver_info
    CASCADE;

DROP TRIGGER IF EXISTS check_speed_trigger ON vehicle_movement_history;
DROP TRIGGER IF EXISTS check_vehicle_type_trigger ON orders;
DROP TRIGGER IF EXISTS check_fuel_expenses_trigger ON fuel_expenses;
DROP TRIGGER IF EXISTS cargo_check_size_trigger ON cargo;
DROP TRIGGER IF EXISTS country_match_check_trigger ON loading_unloading_agreement;
DROP TRIGGER IF EXISTS order_status_sequence_check_trigger ON order_statuses CASCADE;
DROP TRIGGER IF EXISTS order_status_time_check_trigger ON order_statuses CASCADE;
DROP TRIGGER IF EXISTS update_order_status ON driver_status_history CASCADE;
