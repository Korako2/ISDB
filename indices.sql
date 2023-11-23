CREATE INDEX vehicle_movement_history_inx ON vehicle_movement_history (date);

CREATE INDEX order_vehicle_id_idx ON orders(vehicle_id);

CREATE INDEX vehicle_size_idx ON vehicle(length, width);

ALTER TABLE vehicle DROP CONSTRAINT vehicle_plate_number_key;
DROP INDEX vehicle_plate_number_key;
CREATE INDEX vehicle_plate_number_key ON vehicle USING HASH (plate_number);
ALTER TABLE vehicle
ADD CONSTRAINT vehicle_plate_number_key
CHECK (
   plate_number ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{2}$'
   OR plate_number ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{3}$'
);
