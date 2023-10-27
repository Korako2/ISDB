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
  contact_info_type;
