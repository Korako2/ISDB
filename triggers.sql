DROP TRIGGER IF EXISTS check_speed_trigger ON vehicle_movement_history;
CREATE TRIGGER check_speed_trigger
BEFORE INSERT OR UPDATE ON vehicle_movement_history
FOR EACH ROW EXECUTE FUNCTION check_speed();

DROP TRIGGER IF EXISTS check_vehicle_type_trigger ON cargo;
CREATE TRIGGER check_vehicle_type_trigger
BEFORE INSERT OR UPDATE ON cargo
FOR EACH ROW EXECUTE FUNCTION check_vehicle_type();

DROP TRIGGER IF EXISTS check_fuel_expenses_trigger ON fuel_expenses;
CREATE TRIGGER check_fuel_expenses_trigger
BEFORE INSERT ON fuel_expenses
FOR EACH ROW EXECUTE FUNCTION check_fuel_expenses();

DROP TRIGGER IF EXISTS cargo_check_size_trigger ON cargo;
CREATE TRIGGER cargo_check_size_trigger
BEFORE INSERT ON cargo
FOR EACH ROW EXECUTE FUNCTION check_cargo_size();

DROP TRIGGER IF EXISTS country_match_check_trigger ON loading_unloading_agreement;
CREATE TRIGGER country_match_check_trigger
BEFORE INSERT ON loading_unloading_agreement
FOR EACH ROW EXECUTE FUNCTION check_country_match();

DROP TRIGGER IF EXISTS order_status_sequence_check_trigger ON order_statuses CASCADE;
CREATE TRIGGER order_status_sequence_check_trigger
BEFORE INSERT ON order_statuses
FOR EACH ROW EXECUTE FUNCTION check_order_status_sequence();

DROP TRIGGER IF EXISTS order_status_time_check_trigger ON order_statuses CASCADE;
CREATE TRIGGER order_status_time_check_trigger
BEFORE INSERT ON order_statuses
FOR EACH ROW EXECUTE FUNCTION check_order_status_time();

DROP TRIGGER IF EXISTS update_order_status ON driver_status_history CASCADE;
CREATE TRIGGER update_order_status
AFTER INSERT ON driver_status_history
FOR EACH ROW EXECUTE FUNCTION update_order_status();

DROP TRIGGER IF EXISTS check_single_ownership_overlap_trigger ON vehicle_ownership CASCADE;
CREATE TRIGGER check_single_ownership_overlap_trigger
BEFORE INSERT OR UPDATE ON vehicle_ownership
FOR EACH ROW EXECUTE FUNCTION check_single_ownership_overlap();

DROP TRIGGER IF EXISTS check_multiple_ownership_overlap ON vehicle_ownership CASCADE;
CREATE TRIGGER check_multiple_ownership_overlap
BEFORE INSERT OR UPDATE ON vehicle_ownership
FOR EACH ROW EXECUTE FUNCTION check_multiple_ownership_overlap();
