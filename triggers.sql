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


-- Creating the "VEHICLE" table
CREATE TABLE IF NOT EXISTS VEHICLE (
  VEHICLE_ID serial PRIMARY KEY,
  PLATE_NUMBER varchar(9) NOT NULL CHECK (
    PLATE_NUMBER ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{2}$' OR
    PLATE_NUMBER ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{3}$'
  ),
  MODEL varchar(50) NOT NULL,
  MANUFACTURE_YEAR date NOT NULL,
  LENGTH float NOT NULL,
  WIDTH float NOT NULL,
  HEIGHT float NOT NULL,
  LOAD_CAPACITY float NOT NULL,
  BODY_TYPE BODY_TYPE
);

-- Creating the "BODY TYPE" enumeration
CREATE TYPE BODY_TYPE AS ENUM (
  'OPEN',
  'CLOSED'
);

CREATE TYPE CARGO_TYPE AS ENUM (
  'BULK',
  'TIPPER',
  'PALLETIZED'
);

-- Подобранный автомобиль должен соответствовать типу груза. Насыпной, навалочный -- открытый. Тарный -- закрытый.

