DO
'
    DECLARE
    BEGIN
        IF NOT EXISTS (select 1 FROM pg_type WHERE typname = ''driver_status'') THEN
            CREATE TYPE driver_status AS ENUM (
                ''OFF_DUTY'',
                ''ACCEPTED_ORDER'',
                ''EN_ROUTE'',
                ''ARRIVED_AT_LOADING_LOCATION'',
                ''LOADING'',
                ''ARRIVED_AT_UNLOADING_LOCATION'',
                ''UNLOADING'',
                ''COMPLETED_ORDER''
                );
            -- каст нужен был для Spring Data jdbc
            CREATE CAST ( varchar AS driver_status ) WITH INOUT AS IMPLICIT;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''body_type'') THEN
            CREATE TYPE body_type AS ENUM (
                ''OPEN'',
                ''CLOSED''
                );
            CREATE CAST ( varchar AS body_type ) WITH INOUT AS IMPLICIT;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''cargo_type'') THEN
            CREATE TYPE cargo_type AS ENUM (
                ''BULK'',
                ''TIPPER'',
                ''PALLETIZED''
                );
            CREATE CAST ( varchar AS cargo_type ) WITH INOUT AS IMPLICIT;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''order_status'') THEN
            CREATE TYPE order_status AS ENUM (
                ''ACCEPTED'',
                ''ARRIVED_AT_LOADING_LOCATION'',
                ''LOADING'',
                ''ARRIVED_AT_UNLOADING_LOCATION'',
                ''ON_THE_WAY'',
                ''UNLOADING'',
                ''COMPLETED''
                );
            CREATE CAST ( varchar AS order_status ) WITH INOUT AS IMPLICIT;
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = ''contact_info_type'') THEN
            CREATE TYPE contact_info_type AS ENUM (
                ''PHONE NUMBER'',
                ''TELEGRAM'',
                ''EMAIL''
                );
            CREATE CAST ( varchar AS contact_info_type ) WITH INOUT AS IMPLICIT;
        END IF;
    END
' LANGUAGE plpgsql;

CREATE TABLE IF NOT EXISTS person (
  id serial PRIMARY KEY,
  first_name varchar(20) NOT NULL,
  last_name varchar(20) NOT NULL,
  middle_name varchar(20),
  gender char(1) CHECK (gender IN ('M', 'F')) NOT NULL,
  date_of_birth date NOT NULL CHECK (date_of_birth >= '1910-01-01')
);

CREATE TABLE IF NOT EXISTS contact_info (
  person_id int REFERENCES person (id) ON DELETE CASCADE,
  contact_type contact_info_type,
  value text,
  PRIMARY KEY (person_id, contact_type)
);

CREATE TABLE IF NOT EXISTS driver (
  id serial PRIMARY KEY,
  person_id int REFERENCES person(id) ON DELETE CASCADE,
  passport varchar(10) NOT NULL CHECK (passport ~ '^[0-9]{10}$'),
  bank_card_number text NOT NULL
);

CREATE TABLE IF NOT EXISTS customer (
  id serial PRIMARY KEY,
  person_id int REFERENCES person (id) ON DELETE CASCADE,
  organization varchar(50)
);

CREATE TABLE IF NOT EXISTS driver_status_history (
  driver_id int REFERENCES driver (id),
  date timestamp,
  status driver_status,
  PRIMARY KEY (driver_id, date)
);

CREATE TABLE IF NOT EXISTS tariff_rate (
  driver_id int REFERENCES driver (id) ON DELETE CASCADE PRIMARY KEY,
  daily_rate int NOT NULL,
  rate_per_km int NOT NULL
);

CREATE TABLE IF NOT EXISTS driver_license (
  driver_id int REFERENCES driver(id) ON DELETE CASCADE PRIMARY KEY ,
  issue_date date NOT NULL,
  expiration_date date NOT NULL,
  license_number int,
  CONSTRAINT check_issue_expire_dates CHECK (issue_date < expiration_date)
);

CREATE TABLE IF NOT EXISTS vehicle (
  id serial PRIMARY KEY,
  plate_number varchar(9) NOT NULL UNIQUE CHECK (
    plate_number ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{2}$' OR
    plate_number ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{3}$'
  ),
  model varchar(50) NOT NULL,
  manufacture_year date NOT NULL,
  length float NOT NULL,
  width float NOT NULL,
  height float NOT NULL,
  load_capacity float NOT NULL,
  body_type body_type NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle_ownership (
  vehicle_id int REFERENCES vehicle (id) ON DELETE CASCADE,
  driver_id int REFERENCES driver (id) ON DELETE CASCADE,
  ownership_start_date date,
  ownership_end_date date,
  PRIMARY KEY (vehicle_id, driver_id),
  CONSTRAINT check_ownership_dates CHECK (ownership_start_date <= ownership_end_date)
);

CREATE TABLE IF NOT EXISTS vehicle_movement_history (
  vehicle_id int REFERENCES vehicle (id),
  date timestamp,
  latitude float NOT NULL,
  longitude float NOT NULL,
  mileage float NOT NULL,
  PRIMARY KEY (vehicle_id, date)
);

CREATE TABLE IF NOT EXISTS orders (
  id serial PRIMARY KEY,
  customer_id int NOT NULL REFERENCES customer (id) ON DELETE CASCADE,
  distance float NOT NULL,
  price float NOT NULL,
  order_date date NOT NULL,
  vehicle_id int REFERENCES vehicle (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_statuses (
  order_id int REFERENCES orders (id) ON DELETE CASCADE,
  date_time timestamp,
  status order_status,
  PRIMARY KEY (order_id, date_time)
);

CREATE TABLE IF NOT EXISTS cargo (
  id serial PRIMARY KEY,
  weight float NOT NULL CHECK (weight <= 25000),
  width float NOT NULL CHECK (width <= 2.5),
  height float NOT NULL CHECK (height <= 4),
  length float NOT NULL CHECK (length <= 15),
  order_id int NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
  cargo_type cargo_type
);

CREATE TABLE IF NOT EXISTS address (
  id serial PRIMARY KEY,
  country text NOT NULL,
  city text NOT NULL,
  street text NOT NULL,
  building int NOT NULL,
  corpus int
);

CREATE TABLE IF NOT EXISTS storage_point (
  address_id int REFERENCES address(id) ON DELETE CASCADE PRIMARY KEY,
  latitude float NOT NULL CHECK (latitude >= -90 AND latitude <= 90),
  longitude float NOT NULL CHECK (longitude >= -180 AND longitude <= 180)
);

CREATE TABLE IF NOT EXISTS loading_unloading_agreement (
  order_id int REFERENCES orders (id) ON DELETE CASCADE PRIMARY KEY,
  driver_id int NOT NULL REFERENCES driver (id) ON DELETE CASCADE,
  departure_point int NOT NULL REFERENCES storage_point (address_id)  ON DELETE CASCADE,
  delivery_point int NOT NULL REFERENCES storage_point (address_id) ON DELETE CASCADE,
  sender_id int NOT NULL REFERENCES person (id) ON DELETE CASCADE,
  receiver_id int NOT NULL REFERENCES person (id) ON DELETE CASCADE,
  unloading_time time NOT NULL,
  loading_time time NOT NULL,
  CHECK (departure_point <> delivery_point),
  CHECK (
    unloading_time >= '00:01'::time
    AND unloading_time < '24:00'::time
    AND loading_time >= '00:01'::time
    AND loading_time < '24:00'::time
  )
);

CREATE TABLE IF NOT EXISTS fuel_cards_for_drivers (
  driver_id int REFERENCES driver(id) ON DELETE CASCADE,
  fuel_card_number varchar(40) NOT NULL,
  fuel_station_name varchar(50),
  PRIMARY KEY (driver_id, fuel_card_number),
  UNIQUE (fuel_card_number)
);

CREATE TABLE IF NOT EXISTS fuel_expenses (
  fuel_card_number varchar(40) REFERENCES fuel_cards_for_drivers(fuel_card_number) ON DELETE CASCADE,
  date timestamp,
  amount double precision NOT NULL,
  PRIMARY KEY (fuel_card_number, date)
);
