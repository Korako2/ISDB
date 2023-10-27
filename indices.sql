-- мы делаем селекты по дате при проверках трат по карточке. По-дефолту - B-Tree
CREATE INDEX vehicle_movement_history_date_idx ON vehicle_movement_history (date);

-- Индексы на долготу/широту авто
CREATE INDEX vehicle_movement_history_location_idx ON vehicle_movement_history USING btree (longitude, latitude);

CREATE INDEX driver_status_history_status_idx ON driver_status_history USING hash (status);

CREATE INDEX order_statuses_status_idx ON order_statuses USING hash (status);
