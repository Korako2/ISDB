-- triggers & functions

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

-- Подобранный автомобиль должен соответствовать типу груза. Насыпной, навалочный -- открытый. Тарный -- закрытый.
CREATE OR REPLACE FUNCTION check_vehicle_type() RETURNS TRIGGER AS $$
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
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_cargo_size()
RETURNS TRIGGER AS $$
DECLARE
  var_length float;
  var_width float;
  var_height float;
BEGIN
  -- Получаем размеры автомобиля из таблицы заказов
  SELECT
    v.length, v.width, v.height
  INTO
    var_length, var_width, var_height
  FROM
    orders o
  JOIN
    vehicle v ON o.vehicle_id = v.vehicle_id
  WHERE
    o.order_id = NEW.order_id;

  IF NEW.length > var_length OR
     NEW.width > var_width OR
     NEW.height > var_height THEN
    RAISE EXCEPTION 'Размеры груза больше размеров автомобиля';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER cargo_check_size
BEFORE INSERT ON cargo
FOR EACH ROW
EXECUTE PROCEDURE check_cargo_size();


CREATE OR REPLACE FUNCTION check_country_match()
RETURNS TRIGGER AS $$
DECLARE
  departure_country text;
  delivery_country text;
BEGIN
  -- Получаем страну отправления
  SELECT
    a.country
  INTO
    departure_country
  FROM
    address a
  WHERE
    a.address_id = NEW.departure_point;

  -- Получаем страну получения
  SELECT
    a.country
  INTO
    delivery_country
  FROM
    address a
  WHERE
    a.address_id = NEW.delivery_point;

  IF departure_country <> delivery_country THEN
    RAISE EXCEPTION 'Страна отправления и страна получения не совпадают';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER country_match_check
BEFORE INSERT ON loading_unloading_agreement
FOR EACH ROW
EXECUTE FUNCTION check_country_match();


CREATE OR REPLACE FUNCTION check_order_status_sequence()
RETURNS TRIGGER AS $$
DECLARE
  prev_status order_status;
BEGIN
  SELECT
    status
  INTO
    prev_status
  FROM
    order_statuses
  WHERE
    order_id = NEW.order_id
  ORDER BY
    time DESC
  LIMIT 1;

  IF prev_status IS NOT NULL AND
     (prev_status, NEW.status) NOT IN (('ACCEPTED', 'IN PROGRESS'), ('IN PROGRESS', 'ARRIVED AT LOADING LOCATION'), ('ARRIVED AT LOADING LOCATION', 'LOADING'), ('LOADING', 'ARRIVED AT UNLOADING LOCATION'), ('ARRIVED AT UNLOADING LOCATION', 'ON THE WAY'), ('ON THE WAY', 'DELIVERED'), ('DELIVERED', 'COMPLETED')) THEN
    RAISE EXCEPTION 'Неверная последовательность статусов заказа';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_status_sequence_check
BEFORE INSERT ON order_statuses
FOR EACH ROW
EXECUTE FUNCTION check_order_status_sequence();


CREATE OR REPLACE FUNCTION check_order_status_time()
RETURNS TRIGGER AS $$
DECLARE
  prev_time timestamp;
BEGIN
  -- Получаем время предыдущей записи для данного заказа
  SELECT
    MAX(time)
  INTO
    prev_time
  FROM
    order_statuses
  WHERE
    order_id = NEW.order_id;

  -- Проверяем, что новое время больше предыдущего
  IF prev_time IS NOT NULL AND NEW.time <= prev_time THEN
    RAISE EXCEPTION 'Время статуса заказа должно быть больше времени предыдущей записи';
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER order_status_time_check
BEFORE INSERT ON order_statuses
FOR EACH ROW
EXECUTE FUNCTION check_order_status_time();


-- Статусы заказа должны синхронизироваться со статусом водителя
-- on driver_status table update we should add new order_status based on driver_status
CREATE OR REPLACE FUNCTION update_order_status() RETURNS TRIGGER AS $$
BEGIN
  IF NEW.status = 'COMPLETED ORDER' THEN
    INSERT INTO order_statuses (order_id, time, status)
    VALUES (NEW.order_id, NOW(), 'ACCEPTED');
  ELSIF NEW.status = 'EN ROUTE' THEN
    INSERT INTO order_statuses (order_id, time, status)
    VALUES (NEW.order_id, NOW(), 'IN PROGRESS');
  ELSIF NEW.status = 'UNLOADING' THEN
    INSERT INTO order_statuses (order_id, time, status)
    VALUES (NEW.order_id, NOW(), 'ARRIVED AT UNLOADING LOCATION');
  ELSIF NEW.status = 'LOADING' THEN
    INSERT INTO order_statuses (order_id, time, status)
    VALUES (NEW.order_id, NOW(), 'ARRIVED AT LOADING LOCATION');
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_order_status AFTER INSERT ON driver_status_history
FOR EACH ROW EXECUTE PROCEDURE update_order_status();
