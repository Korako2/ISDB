-- triggers & functions
-- Ограничение по макс. изменению расстояния. Скорость авто не может быть больше 170км/ч

-- Creating the "VEHICLE MOVEMENT HISTORY" table
-- CREATE TABLE IF NOT EXISTS VEHICLE_MOVEMENT_HISTORY (
--   VEHICLE_ID int REFERENCES VEHICLE (VEHICLE_ID),
--   DATE timestamp,
--   LATITUDE float NOT NULL,
--   LONGITUDE float NOT NULL,
--   MILEAGE float NOT NULL,
--   PRIMARY KEY (VEHICLE_ID, DATE)
-- );

-- calculate difference between new and last distance for current VEHICLE_ID and divide by date difference
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

