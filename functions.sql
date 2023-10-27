CREATE EXTENSION IF NOT EXISTS cube;
CREATE EXTENSION IF NOT EXISTS earthdistance;

-- функция добавления заказа в систему.
CREATE OR REPLACE FUNCTION add_order(
    var_customer_id int,
    address_a_id int,
    address_b_id int,
    var_vehicle_id int
) RETURNS int AS
$$
DECLARE
    calculated_distance float;
    calculated_price    float;
    order_id            int;
BEGIN
    SELECT (
               point(a.latitude, a.longitude) <@> point(b.latitude, b.longitude)
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

    INSERT INTO order_statuses (order_id, time, status)
    VALUES (order_id, NOW(), 'ACCEPTED');

    RETURN order_id;
END;
$$ LANGUAGE plpgsql;

-- Функция добавления заказчика
CREATE OR REPLACE FUNCTION add_customer(
    v_first_name varchar(20),
    v_last_name varchar(20),
    v_gender char(1),
    v_date_of_birth date,
    v_middle_name varchar(20) default null,
    v_organization varchar(50) default null
) RETURNS int AS $$
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
    RETURNING person_id INTO v_person_id;

    INSERT INTO customer (person_id, organization)
    VALUES (v_person_id, v_organization)
    RETURNING customer_id INTO customer_id;

    RETURN customer_id;
END;
$$ LANGUAGE plpgsql;
