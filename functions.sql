-- функция добавления заказа в систему.
CREATE OR REPLACE FUNCTION add_order(
    var_customer_id int,
    distance float,
    var_vehicle_id int,
    v_weight float,
    v_width float,
    v_height float,
    v_length float,
    v_cargo_type cargo_type
) RETURNS int AS $ord_id$
DECLARE
    calculated_price    float;
    ord_id            int;
BEGIN
    calculated_price = distance * 20;
    INSERT INTO orders (customer_id, distance, price, order_date, vehicle_id)
    VALUES (var_customer_id, distance, calculated_price, NOW(), var_vehicle_id)
    RETURNING order_id INTO ord_id;

    INSERT INTO order_statuses (order_id, date_time, status)
    VALUES (ord_id, NOW(), 'ACCEPTED');

    INSERT INTO cargo (weight, width, height, length, order_id, cargo_type)
    VALUES (v_weight, v_width, v_height, v_length, ord_id, v_cargo_type);
    RETURN ord_id;
END;
$ord_id$ LANGUAGE plpgsql;

-- Функция добавления нового заказчика
CREATE OR REPLACE FUNCTION add_new_customer(
    v_first_name varchar(20),
    v_last_name varchar(20),
    v_gender char(1),
    v_date_of_birth date,
    v_middle_name varchar(20) default null,
    v_organization varchar(50) default null
) RETURNS int AS $customer_id$
DECLARE
    v_person_id int;
    v_customer_id int;
BEGIN
    INSERT INTO person (first_name, last_name, middle_name, gender, date_of_birth)
    VALUES (v_first_name, v_last_name, v_middle_name, v_gender, v_date_of_birth)
    RETURNING person_id INTO v_person_id;

    INSERT INTO customer (person_id, organization)
    VALUES (v_person_id, v_organization)
    RETURNING customer_id INTO v_customer_id;

    RETURN v_customer_id;
END;
$customer_id$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION check_speed() RETURNS TRIGGER AS $check_speed$
DECLARE
  prev_record float;
  prev_date timestamp;
  speed float;
BEGIN
  prev_record = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID ORDER BY DATE DESC LIMIT 1);
  prev_date = (SELECT DATE FROM vehicle_movement_history WHERE VEHICLE_ID = NEW.VEHICLE_ID ORDER BY DATE DESC LIMIT 1);
  speed = (NEW.MILEAGE - prev_record) / (extract(EPOCH FROM NEW.DATE - prev_date) / 60.0);

  IF speed > 170 THEN
    RAISE EXCEPTION 'Speed cannot be more than 170 km/h';
  END IF;
  RETURN NEW;
END;
$check_speed$ LANGUAGE plpgsql;

-- Подобранный автомобиль должен соответствовать типу груза. Насыпной, навалочный -- открытый. Тарный -- закрытый.
CREATE OR REPLACE FUNCTION check_vehicle_type() RETURNS TRIGGER AS $check_vehicle_type$
DECLARE
    var_cargo_id int;
    var_cargo_type text;
    var_body_type text;
BEGIN
  SELECT cargo_id INTO var_cargo_id FROM cargo WHERE order_id = NEW.order_id;
  SELECT cargo_type INTO var_cargo_type FROM cargo WHERE cargo.cargo_id = var_cargo_id;
  SELECT body_type INTO var_body_type FROM vehicle WHERE vehicle.vehicle_id = NEW.vehicle_id;
  IF var_cargo_type = 'BULK' OR var_cargo_type = 'TIPPER' THEN
    IF var_body_type != 'OPEN' THEN
      RAISE EXCEPTION 'Vehicle type must be OPEN';
    END IF;
  END IF;
  IF var_cargo_type = 'PALLETIZED' THEN
    IF var_body_type != 'CLOSED' THEN
      RAISE EXCEPTION 'Vehicle type must be CLOSED';
    END IF;
  END IF;
  RETURN NEW;
END;
$check_vehicle_type$ LANGUAGE plpgsql;

-- расходы должны сопоставляться с пробегом автомобиля
CREATE OR REPLACE FUNCTION check_fuel_expenses() RETURNS TRIGGER AS $check_fuel_expenses$
DECLARE
  prev_pay_date timestamp;
  prev_mileage float;
  current_mileage float;
  var_vehicle_id int;
BEGIN
  var_vehicle_id = (SELECT vehicle_id FROM vehicle WHERE vehicle_id =
       (SELECT vehicle_id FROM vehicle_ownership WHERE vehicle_ownership.driver_id =
           (SELECT fuel_cards_for_drivers.driver_id FROM fuel_cards_for_drivers WHERE fuel_card_number = NEW.FUEL_CARD_NUMBER)));
  -- select record from movement history nearest to prev_pay_date
  prev_pay_date = (SELECT DATE FROM FUEL_EXPENSES WHERE FUEL_CARD_NUMBER = NEW.FUEL_CARD_NUMBER ORDER BY DATE DESC LIMIT 1);
  prev_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = var_vehicle_id AND DATE <= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  current_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = var_vehicle_id AND DATE >= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  IF (current_mileage - prev_mileage) * 6 < NEW.AMOUNT THEN
    RAISE EXCEPTION 'Fuel expenses are too high';
  END IF;
  RETURN NEW;
END;
$check_fuel_expenses$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_cargo_size() RETURNS TRIGGER AS $check_cargo_size$
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
$check_cargo_size$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_country_match() RETURNS TRIGGER AS $check_country_match$
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
$check_country_match$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION check_order_status_sequence() RETURNS TRIGGER AS $check_order_status_sequence$
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
     (prev_status, NEW.status) NOT IN (('ACCEPTED', 'ARRIVED AT LOADING LOCATION'), ('ARRIVED AT LOADING LOCATION', 'LOADING'), ('LOADING', 'ON THE WAY'), ('ON THE WAY', 'ARRIVED AT UNLOADING LOCATION'), ('ARRIVED AT UNLOADING LOCATION', 'UNLOADING'), ('UNLOADING', 'COMPLETED')) THEN
    RAISE EXCEPTION 'Неверная последовательность статусов заказа';
  END IF;

  RETURN NEW;
END;
$check_order_status_sequence$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_order_status_time() RETURNS TRIGGER AS $check_order_status_time$
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
    RAISE EXCEPTION 'Время статуса заказа должно быть больше времени предыдущей записи';
  END IF;

  RETURN NEW;
END;
$check_order_status_time$ LANGUAGE plpgsql;

-- Статусы заказа должны синхронизироваться со статусом водителя
CREATE OR REPLACE FUNCTION update_order_status() RETURNS TRIGGER AS $update_order_status$
DECLARE
    current_order_id int;
BEGIN
    current_order_id = (SELECT order_id FROM orders WHERE vehicle_id = (
        SELECT vehicle_id FROM vehicle_ownership WHERE vehicle_ownership.driver_id = NEW.driver_id AND ownership_end_date IS NULL
    ));
    IF current_order_id IS NULL THEN
        RAISE EXCEPTION 'Заказ не существует или авто не назначен';
    END IF;

    IF NEW.status = 'ACCEPTED ORDER' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'ACCEPTED');
    ELSIF NEW.status = 'ARRIVED AT LOADING LOCATION' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'ARRIVED AT LOADING LOCATION');
    ELSIF NEW.status = 'LOADING' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'LOADING');
    ELSIF NEW.status = 'EN ROUTE' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'ON THE WAY');
    ELSIF NEW.status = 'ARRIVED AT UNLOADING LOCATION' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'ARRIVED AT UNLOADING LOCATION');
    ELSIF NEW.status = 'UNLOADING' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'UNLOADING');
    ELSIF NEW.status = 'COMPLETED ORDER' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, 'COMPLETED');
    END IF;
    RETURN NEW;

END;
$update_order_status$ LANGUAGE plpgsql;

-- формула гаверсинусов
--    SELECT 2 * 6371 * ASIN(
--        SQRT(
--            POWER(SIN(RADIANS(b.latitude - a.latitude) / 2), 2) +
--            COS(RADIANS(a.latitude)) * COS(RADIANS(b.latitude)) *
--            POWER(SIN(RADIANS(b.longitude - a.longitude) / 2), 2)
--        )
--    )

--    FROM storage_point a,
--         storage_point b
--    WHERE a.address_id = address_a_id
--      AND b.address_id = address_b_id
--    INTO calculated_distance;

-- Функция добавления заказчика
CREATE OR REPLACE FUNCTION add_customer(
    v_person_id int,
    v_organization varchar(50) default null
) RETURNS int AS $customer_id$
DECLARE
    v_customer_id int;
BEGIN
    -- Проверка, что заказчика с person_id = v_person_id не существует
    IF EXISTS (SELECT 1 FROM customer WHERE person_id = v_person_id) THEN
        RAISE EXCEPTION 'Заказчик с person_id = % уже существует', v_person_id;
    ELSE
        INSERT INTO customer (person_id, organization)
        VALUES (v_person_id, v_organization)
        RETURNING customer_id INTO v_customer_id;
    END IF;

    RETURN v_customer_id;
END;
$customer_id$ LANGUAGE plpgsql;

-- Функция добавления водителя
CREATE OR REPLACE FUNCTION add_driver(
    v_first_name varchar(20),
    v_last_name varchar(20),
    v_middle_name varchar(20),
    v_gender char(1),
    v_date_of_birth date,
    v_passport varchar(10),
    v_bank_card_number text
) RETURNS int AS $driver_id$
DECLARE
    v_person_id int;
    v_driver_id int;
BEGIN
    -- Добавляем запись в таблицу person
    INSERT INTO person (first_name, last_name, middle_name, gender, date_of_birth)
    VALUES (v_first_name, v_last_name, v_middle_name, v_gender, v_date_of_birth)
    RETURNING person_id INTO v_person_id;

    -- Добавляем запись в таблицу driver
    INSERT INTO driver (person_id, passport, bank_card_number)
    VALUES (v_person_id, v_passport, v_bank_card_number)
    RETURNING driver_id INTO v_driver_id;

    RETURN v_driver_id;
END;
$driver_id$ LANGUAGE plpgsql;


