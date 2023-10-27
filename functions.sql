create extension cube;
create extension earthdistance;

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

    -- Расчет заработной платы за произведенную поездку рассчитывается по формуле - P + S * T,  где  S– расстояние, которое проехал водитель, T – стоимость одного километра пути, P - суточные.
    SELECT calculated_distance * rate_per_km + daily_rate
    FROM tariff_rate
    WHERE driver_id = (SELECT driver_id
                       FROM vehicle_ownership
                       WHERE vehicle_ownership.vehicle_id = var_vehicle_id)
    INTO calculated_price;

    INSERT INTO orders (customer_id, distance, price, order_date, vehicle_id)
    VALUES (var_customer_id, calculated_distance, calculated_price, NOW(), var_vehicle_id)
    RETURNING order_id INTO order_id;

    RETURN order_id;
END;
$$ LANGUAGE plpgsql;
