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
) RETURNS int AS '
DECLARE
    calculated_price    float;
    ord_id            int;
BEGIN
    calculated_price = distance * 20;
    INSERT INTO orders (customer_id, distance, price, order_date, vehicle_id)
    VALUES (var_customer_id, distance, calculated_price, NOW(), var_vehicle_id)
    RETURNING id INTO ord_id;

    INSERT INTO order_statuses (order_id, date_time, status)
    VALUES (ord_id, NOW(), ''ACCEPTED'');

    INSERT INTO cargo (weight, width, height, length, order_id, cargo_type)
    VALUES (v_weight, v_width, v_height, v_length, ord_id, v_cargo_type);
    RETURN ord_id;
END
' LANGUAGE plpgsql;

-- Функция добавления нового заказчика
CREATE OR REPLACE FUNCTION add_new_customer(
    v_first_name varchar(20),
    v_last_name varchar(20),
    v_gender char(1),
    v_date_of_birth date,
    v_middle_name varchar(20) default null,
    v_organization varchar(50) default null
) RETURNS int AS '
DECLARE
    v_person_id int;
    v_customer_id int;
BEGIN
    INSERT INTO person (first_name, last_name, middle_name, gender, date_of_birth)
    VALUES (v_first_name, v_last_name, v_middle_name, v_gender, v_date_of_birth)
    RETURNING id INTO v_person_id;

    INSERT INTO customer (person_id, organization)
    VALUES (v_person_id, v_organization)
    RETURNING id INTO v_customer_id;

    RETURN v_customer_id;
END
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

-- расходы должны сопоставляться с пробегом автомобиля
CREATE OR REPLACE FUNCTION check_fuel_expenses() RETURNS TRIGGER AS '
DECLARE
  prev_pay_date timestamp;
  prev_mileage float;
  current_mileage float;
  var_vehicle_id int;
BEGIN
  SELECT id INTO var_vehicle_id
  FROM vehicle
  JOIN vehicle_ownership vo ON vehicle.id = vo.vehicle_id
  JOIN fuel_cards_for_drivers fc ON vo.driver_id = fc.driver_id
  WHERE fc.fuel_card_number = NEW.FUEL_CARD_NUMBER
  ORDER BY vo.ownership_end_date DESC
  LIMIT 1;
  -- select record from movement history nearest to prev_pay_date
  prev_pay_date = (SELECT DATE FROM FUEL_EXPENSES WHERE FUEL_CARD_NUMBER = NEW.FUEL_CARD_NUMBER ORDER BY DATE DESC LIMIT 1);
  prev_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = var_vehicle_id AND DATE <= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  current_mileage = (SELECT MILEAGE FROM vehicle_movement_history WHERE VEHICLE_ID = var_vehicle_id AND DATE >= prev_pay_date ORDER BY DATE DESC LIMIT 1);
  IF (current_mileage - prev_mileage) * 6 < NEW.AMOUNT THEN
    RAISE EXCEPTION ''Fuel expenses are too high'';
  END IF;
  RETURN NEW;
END
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
END
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
END
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
     (prev_status, NEW.status) NOT IN ((''ACCEPTED'', ''ARRIVED_AT_LOADING_LOCATION''), (''ARRIVED_AT_LOADING_LOCATION'', ''LOADING''), (''LOADING'', ''ON_THE_WAY''), (''ON_THE_WAY'', ''ARRIVED_AT_UNLOADING_LOCATION''), (''ARRIVED_AT_UNLOADING_LOCATION'', ''UNLOADING''), (''UNLOADING'', ''COMPLETED'')) THEN
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
        RAISE EXCEPTION ''Заказ не существует или авто не назначен'';
    END IF;

    IF NEW.status = ''ACCEPTED_ORDER'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''ACCEPTED'');
    ELSIF NEW.status = ''ARRIVED_AT_LOADING_LOCATION'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''ARRIVED_AT_LOADING_LOCATION'');
    ELSIF NEW.status = ''LOADING'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''LOADING'');
    ELSIF NEW.status = ''EN_ROUTE'' THEN
        INSERT INTO order_statuses (order_id, date_time, status)
        VALUES (current_order_id, NEW.date, ''ON_THE_WAY'');
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
) RETURNS int AS '
DECLARE
    v_customer_id int;
BEGIN
    -- Проверка, что заказчика с person_id = v_person_id не существует
    IF EXISTS (SELECT 1 FROM customer WHERE person_id = v_person_id) THEN
        RAISE EXCEPTION ''Заказчик с person_id = % уже существует'', v_person_id;
    ELSE
        INSERT INTO customer (person_id, organization)
        VALUES (v_person_id, v_organization)
        RETURNING id INTO v_customer_id;
    END IF;

    RETURN v_customer_id;
END;
' LANGUAGE plpgsql;

-- Функция добавления водителя
CREATE OR REPLACE FUNCTION add_driver(
    v_first_name varchar(20),
    v_last_name varchar(20),
    v_middle_name varchar(20),
    v_gender char(1),
    v_date_of_birth date,
    v_passport varchar(10),
    v_bank_card_number text
) RETURNS int AS '
DECLARE
    v_person_id int;
    v_driver_id int;
BEGIN
    -- Добавляем запись в таблицу person
    INSERT INTO person (first_name, last_name, middle_name, gender, date_of_birth)
    VALUES (v_first_name, v_last_name, v_middle_name, v_gender, v_date_of_birth)
    RETURNING id INTO v_person_id;

    -- Добавляем запись в таблицу driver
    INSERT INTO driver (person_id, passport, bank_card_number)
    VALUES (v_person_id, v_passport, v_bank_card_number)
    RETURNING id INTO v_driver_id;

    RETURN v_driver_id;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_vehicle(
    v_plate_number varchar(9),
    v_model varchar(50),
    v_manufacture_year date,
    v_length float,
    v_width float,
    v_height float,
    v_load_capacity float,
    v_body_type body_type,
    v_driver_id int,
    v_ownership_start_date date
) RETURNS int AS '
DECLARE
    v_vehicle_id int;
BEGIN
    -- Добавляем запись в таблицу vehicle
    INSERT INTO vehicle (plate_number, model, manufacture_year, length, width, height, load_capacity, body_type)
    VALUES (v_plate_number, v_model, v_manufacture_year, v_length, v_width, v_height, v_load_capacity, v_body_type)
    RETURNING id INTO v_vehicle_id;

    -- Устанавливаем связь с водителем в таблице vehicle_ownership
    INSERT INTO vehicle_ownership (vehicle_id, driver_id, ownership_start_date)
    VALUES (v_vehicle_id, v_driver_id, v_ownership_start_date);

    RETURN v_vehicle_id;
END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION add_driver_info(
    v_driver_id int,
    v_daily_rate int,
    v_rate_per_km int,
    v_issue_date date,
    v_expiration_date date,
    v_license_number int,
    v_fuel_cards text[],
    v_fuel_station_names text[]
) RETURNS void AS '
DECLARE
    fuel_card text;
    station_name text;
BEGIN
    -- Добавляем тарифную ставку
    INSERT INTO tariff_rate (driver_id, daily_rate, rate_per_km)
    VALUES (v_driver_id, v_daily_rate, v_rate_per_km);

    -- Добавляем водительское удостоверение
    INSERT INTO driver_license (driver_id, issue_date, expiration_date, license_number)
    VALUES (v_driver_id, v_issue_date, v_expiration_date, v_license_number);

    -- Добавляем топливные карты и названия заправочных станций
    FOR i IN 1..GREATEST(array_length(v_fuel_cards, 1), array_length(v_fuel_station_names, 1)) LOOP
        fuel_card := v_fuel_cards[i];
        station_name := v_fuel_station_names[i];

        INSERT INTO fuel_cards_for_drivers (driver_id, fuel_card_number, fuel_station_name)
        VALUES (v_driver_id, fuel_card, station_name);
    END LOOP;

    RETURN;
END
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION find_car_to_fit_size(
    v_length float,
    v_width float,
    v_height float,
    v_cargo_type cargo_type,
    v_weight float
) RETURNS TABLE (
    vehicle_id int
) AS '
BEGIN
    RETURN QUERY
    SELECT
        v.id
    FROM
        vehicle v
    WHERE
        v.length >= v_length
        AND v.width >= v_width
        AND v.height >= v_height
        AND (
            (v_cargo_type = ''BULK'' AND v.body_type = ''OPEN'') OR
            (v_cargo_type = ''TIPPER'' AND v.body_type = ''OPEN'') OR
            (v_cargo_type = ''PALLETIZED'' AND v.body_type = ''CLOSED'')
        )
        AND v.load_capacity >= v_weight;

END
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION find_suitable_vehicle(
    v_length FLOAT,
    v_width FLOAT,
    v_height FLOAT,
    v_cargo_type VARCHAR,
    v_weight FLOAT,
    cargo_latitude FLOAT,
    cargo_longitude FLOAT
)
RETURNS TABLE (
    closest_vehicle_id INT,
    distance FLOAT
) AS $$
DECLARE
    suitable_vehicles CURSOR FOR
        SELECT *
        FROM find_car_to_fit_size(v_length, v_width, v_height, v_cargo_type, v_weight);
    current_vehicle RECORD;
    closest_vehicle_id INT := -1;
    closest_distance FLOAT := 999999;
    current_distance FLOAT;
BEGIN
    OPEN suitable_vehicles;
    LOOP
        FETCH suitable_vehicles INTO current_vehicle;
        EXIT WHEN NOT FOUND;

        -- Выбор самых последних координат для текущего автомобиля
        SELECT
            2 * 6371 * ASIN(
                SQRT(
                    POWER(SIN(RADIANS(vmh.latitude - cargo_latitude) / 2), 2) +
                    COS(RADIANS(cargo_latitude)) * COS(RADIANS(vmh.latitude)) *
                    POWER(SIN(RADIANS(vmh.longitude - cargo_longitude) / 2), 2)
                )
            ) INTO current_distance
        FROM (
            SELECT
                vmh.*,
                ROW_NUMBER() OVER (PARTITION BY vmh.vehicle_id ORDER BY vmh.date DESC) AS rn
            FROM
                vehicle_movement_history vmh
            WHERE
                vmh.vehicle_id = current_vehicle.vehicle_id
        ) vmh
        WHERE
            vmh.rn = 1;

        -- Если текущее расстояние меньше самого близкого, обновляем значения
        IF current_distance < closest_distance THEN
            closest_vehicle_id := current_vehicle.vehicle_id;
            closest_distance := current_distance;
        END IF;
    END LOOP;

    CLOSE suitable_vehicles;
    -- Возвращаем ID самого близкого автомобиля и расстояние до него
    RETURN QUERY SELECT closest_vehicle_id, closest_distance;
END;
$$ LANGUAGE plpgsql;

-- function checks that multiple drivers don't own the same vehicle in a time period
CREATE OR REPLACE FUNCTION check_multiple_ownership_overlap() RETURNS TRIGGER AS '
BEGIN
    IF EXISTS (
        SELECT 1
        FROM vehicle_ownership
        WHERE vehicle_id = NEW.vehicle_id
            AND driver_id != NEW.driver_id
            AND ((ownership_start_date, ownership_end_date) OVERLAPS (NEW.ownership_start_date, NEW.ownership_end_date))
    ) THEN
        RAISE EXCEPTION ''Trying to add new driver to a vehicle but owning date range overlap'';
    END IF;

    RETURN NEW;
END
' LANGUAGE plpgsql;



