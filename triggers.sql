-- triggers & functions
-- Ограничение по макс. изменению расстояния. Скорость авто не может быть больше 170км/ч

CREATE OR REPLACE FUNCTION check_speed() RETURNS TRIGGER AS $$
DECLARE
  prev_record float;
  prev_date timestamp;
  speed float;
BEGIN
  prev_record = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID ORDER BY DATE DESC LIMIT 1);
  prev_date = (SELECT DATE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID ORDER BY DATE DESC LIMIT 1);
  -- count difference in hours between two timestamps
  -- debug prev_record
  RAISE NOTICE 'Value: %', prev_record;
  RAISE NOTICE 'Value: %', prev_date;

  speed = (NEW.MILEAGE - prev_record) / (EXTRACT(EPOCH FROM (NEW.DATE - prev_date)) / 3600);
  IF speed > 170 THEN
    RAISE EXCEPTION 'Speed cannot be more than 170 km/h';
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_speed_trigger
  BEFORE INSERT OR UPDATE ON vehicle_movement_history
  FOR EACH ROW EXECUTE PROCEDURE check_speed();

-- Подобранный автомобиль должен соответствовать типу груза. Насыпной, навалочный -- открытый. Тарный -- закрытый.
CREATE OR REPLACE FUNCTION check_vehicle_type RETURNS TRIGGER AS $$
BEGIN
  IF NEW.CARGO_TYPE = 'BULK' OR NEW.CARGO_TYPE = 'TIPPER' THEN
    IF NEW.BODY_TYPE != 'OPEN' THEN
      RAISE EXCEPTION 'Vehicle type must be OPEN';
    END IF;
  END IF;
  IF NEW.CARGO_TYPE = 'PALLETIZED' THEN
    IF NEW.BODY_TYPE != 'CLOSED' THEN
      RAISE EXCEPTION 'Vehicle type must be CLOSED';
    END IF;
  END IF;
  RETURN NEW;
END;
END;
$$ LANGUAGE plpgsql;

-- расходы должны сопоставляться с пробегом автомобиля
-- Fuel price is 60 RUB per liter. Fuel consumption is 10 liters per 100 km. So 6 RUB per km is the maximum allowed value
CREATE OR REPLACE FUNCTION check_fuel_expenses() RETURNS TRIGGER AS $$
DECLARE
  prev_pay_date timestamp;
  prev_mileage float;
  current_mileage float;
BEGIN
  -- select record from movement history nearest to prev_pay_date
  prev_pay_date = (SELECT DATE FROM FUEL_EXPENSES WHERE FUEL_CARD_NUMBER = NEW.FUEL_CARD_NUMBER ORDER BY DATE DESC LIMIT 1);
  prev_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID AND DATE <= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  current_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID AND DATE >= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  IF (current_mileage - prev_mileage) * 6 < NEW.AMOUNT THEN
    RAISE EXCEPTION 'Fuel expenses are too high';
  END IF;

  RETURN NEW;
END;
