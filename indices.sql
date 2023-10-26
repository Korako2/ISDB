-- мы делаем селекты по дате при проверках трат по карточке. По-дефолту - B-Tree
CREATE INDEX vehicle_movement_history_date_idx ON vehicle_movement_history (date);
