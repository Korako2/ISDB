-- функция добавления заказа в систему.
CREATE OR REPLACE FUNCTION add_order(
    var_customer_id int,
    address_a_id int,
    address_b_id int,
    var_vehicle_id int
) RETURNS int AS
'
DECLARE
    calculated_distance float;
    calculated_price    float;
    order_id            int;
BEGIN
    SELECT (
      a.latitude - b.latitude
    )
    FROM storage_point a,
         storage_point b
    WHERE a.address_id = address_a_id
      AND b.address_id = address_b_id
    INTO calculated_distance;

    SELECT 2 * (calculated_distance * rate_per_km + daily_rate)
    FROM tariff_rate
    WHERE driver_id = (SELECT driver_id
                       FROM vehicle_ownership
                       WHERE vehicle_ownership.vehicle_id = var_vehicle_id)
    INTO calculated_price;

    INSERT INTO orders (customer_id, distance, price, order_date, vehicle_id)
    VALUES (var_customer_id, calculated_distance, calculated_price, NOW(), var_vehicle_id)
    RETURNING order_id INTO order_id;

    INSERT INTO order_statuses (order_id, date_time, status)
    VALUES (order_id, NOW(), ''ACCEPTED'');

    RETURN order_id;
END
'
LANGUAGE plpgsql;

-- Функция добавления заказчика
CREATE OR REPLACE FUNCTION add_customer(
    v_first_name varchar(20),
    v_last_name varchar(20),
    v_gender char(1),
    v_date_of_birth date,
    v_middle_name varchar(20) default null,
    v_organization varchar(50) default null
) RETURNS int AS '
DECLARE
    v_person_id int;
    customer_id int;
BEGIN
    INSERT INTO person
    select *
    from  (
      VALUES (v_first_name, v_last_name, v_middle_name, v_gender, v_date_of_birth)
    ) as data(first_name, last_name, middle_name, gender, date_of_birth)
    WHERE NOT EXISTS (
      select *
      from person
      where data = person
    )
    RETURNING id INTO v_person_id;

    INSERT INTO customer (person_id, organization)
    VALUES (v_person_id, v_organization)
    RETURNING customer_id INTO customer_id;

    RETURN customer_id;
END;
' LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_speed() RETURNS TRIGGER AS '
DECLARE
  prev_record float;
  prev_date timestamp;
  speed float;
BEGIN
  prev_record = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID ORDER BY DATE DESC LIMIT 1);
  prev_date = (SELECT DATE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID ORDER BY DATE DESC LIMIT 1);
  speed = (NEW.MILEAGE - prev_record) / (extract(EPOCH FROM NEW.DATE - prev_date) / 60.0);

  IF speed > 170 THEN
    RAISE EXCEPTION ''Speed cannot be more than 170 km/h'';
  END IF;
  RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Подобранный автомобиль должен соответствовать типу груза. Насыпной, навалочный -- открытый. Тарный -- закрытый.
CREATE OR REPLACE FUNCTION check_vehicle_type() RETURNS TRIGGER AS '
DECLARE
    var_cargo_id int;
    var_cargo_type text;
    var_body_type text;
BEGIN
  SELECT id INTO var_cargo_id FROM cargo WHERE order_id = NEW.id;
  SELECT cargo_type INTO var_cargo_type FROM cargo WHERE cargo.id = var_cargo_id;
  SELECT body_type INTO var_body_type FROM vehicle WHERE vehicle.id = NEW.vehicle_id;
  IF var_cargo_type = ''BULK'' OR var_cargo_type = ''TIPPER'' THEN
    IF var_body_type != ''OPEN'' THEN
      RAISE EXCEPTION ''Vehicle type must be OPEN'';
    END IF;
  END IF;
  IF var_cargo_type = ''PALLETIZED'' THEN
    IF var_body_type != ''CLOSED'' THEN
      RAISE EXCEPTION ''Vehicle type must be CLOSED'';
    END IF;
  END IF;
  RETURN NEW;
END;
' LANGUAGE plpgsql;

-- расходы должны сопоставляться с пробегом автомобиля
CREATE OR REPLACE FUNCTION check_fuel_expenses() RETURNS TRIGGER AS '
DECLARE
  prev_pay_date timestamp;
  prev_mileage float;
  current_mileage float;
  var_vehicle_id int;
BEGIN
  var_vehicle_id = (SELECT id FROM vehicle WHERE id =
       (SELECT vehicle_id FROM vehicle_ownership WHERE vehicle_ownership.driver_id =
           (SELECT fuel_cards_for_drivers.driver_id FROM fuel_cards_for_drivers WHERE fuel_card_number = NEW.FUEL_CARD_NUMBER)));
  -- select record from movement history nearest to prev_pay_date
  prev_pay_date = (SELECT DATE FROM FUEL_EXPENSES WHERE FUEL_CARD_NUMBER = NEW.FUEL_CARD_NUMBER ORDER BY DATE DESC LIMIT 1);
  prev_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = var_vehicle_id AND DATE <= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  current_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = var_vehicle_id AND DATE >= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  IF (current_mileage - prev_mileage) * 6 < NEW.AMOUNT THEN
    RAISE EXCEPTION ''Fuel expenses are too high'';
  END IF;
  RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_cargo_size() RETURNS TRIGGER AS '
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
    vehicle v ON o.vehicle_id = v.id
  WHERE
    o.id = NEW.order_id;

  IF NEW.length > var_length OR
     NEW.width > var_width OR
     NEW.height > var_height THEN
    RAISE EXCEPTION ''Размеры груза больше размеров автомобиля'';
  END IF;

  RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_country_match() RETURNS TRIGGER AS '
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
    a.id = NEW.departure_point;

  -- Получаем страну получения
  SELECT
    a.country
  INTO
    delivery_country
  FROM
    address a
  WHERE
    a.id = NEW.delivery_point;

  IF departure_country <> delivery_country THEN
    RAISE EXCEPTION ''Страна отправления и страна получения не совпадают'';
  END IF;

  RETURN NEW;
END;
' LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_order_status_sequence() RETURNS TRIGGER AS '
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
    date_time DESC
  LIMIT 1;

  IF prev_status IS NOT NULL AND
     (prev_status, NEW.status) NOT IN ((''ACCEPTED'', ''IN_PROGRESS''), (''IN_PROGRESS'', ''ARRIVED_AT_LOADING_LOCATION''), (''ARRIVED_AT_LOADING_LOCATION'', ''LOADING''), (''LOADING'', ''ARRIVED_AT_UNLOADING_LOCATION''), (''ARRIVED_AT_UNLOADING_LOCATION'', ''ON_THE_WAY''), (''ON_THE_WAY'', ''UNLOADING''), (''UNLOADING'', ''COMPLETED'')) THEN
    RAISE EXCEPTION ''Неверная последовательность статусов заказа'';
  END IF;

  RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_order_status_time() RETURNS TRIGGER AS '
DECLARE
  prev_time timestamp;
BEGIN
  -- Получаем время предыдущей записи для данного заказа
  SELECT
    MAX(date_time)
  INTO
    prev_time
  FROM
    order_statuses
  WHERE
    order_id = NEW.order_id;

  -- Проверяем, что новое время больше предыдущего
  IF prev_time IS NOT NULL AND NEW.date_time <= prev_time THEN
    RAISE EXCEPTION ''Время статуса заказа должно быть больше времени предыдущей записи'';
  END IF;

  RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Статусы заказа должны синхронизироваться со статусом водителя
CREATE OR REPLACE FUNCTION update_order_status() RETURNS TRIGGER AS '
DECLARE
    current_order_id int;
BEGIN
    current_order_id = (SELECT id FROM orders WHERE vehicle_id = (
        SELECT vehicle_id FROM vehicle_ownership WHERE vehicle_ownership.driver_id = NEW.driver_id AND ownership_end_date IS NULL
    ));
    IF current_order_id IS NULL THEN
        RAISE EXCEPTION ''Заказ не существует или авто не назначен или не связан с водителем (%)'', NEW.driver_id;
    END IF;

    IF NEW.status = ''ACCEPTED_ORDER'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''ACCEPTED'');
    ELSIF NEW.status = ''EN_ROUTE'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''IN_PROGRESS'');
    ELSIF NEW.status = ''ARRIVED_AT_LOADING_LOCATION'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''ARRIVED_AT_LOADING_LOCATION'');
    ELSIF NEW.status = ''LOADING'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''LOADING'');
    ELSIF NEW.status = ''ARRIVED_AT_UNLOADING_LOCATION'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''ARRIVED_AT_UNLOADING_LOCATION'');
    ELSIF NEW.status = ''UNLOADING'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''UNLOADING'');
    ELSIF NEW.status = ''COMPLETED_ORDER'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''COMPLETED'');
    END IF;
    RETURN NEW;
END;
' LANGUAGE plpgsql;