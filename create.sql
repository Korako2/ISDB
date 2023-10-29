DROP TYPE IF EXISTS driver_status CASCADE;
CREATE TYPE driver_status AS ENUM (
  'OFF DUTY',
  'EN ROUTE',
  'UNLOADING',
  'LOADING',
  'WAITING FOR ORDER',
  'WAITING FOR LOADING',
  'WAITING FOR UNLOADING',
  'ARRIVED AT LOADING LOCATION',
  'COMPLETED ORDER'
);

DROP TYPE IF EXISTS body_type CASCADE;
CREATE TYPE body_type AS ENUM (
  'OPEN',
  'CLOSED'
);

DROP TYPE IF EXISTS cargo_type CASCADE;
CREATE TYPE cargo_type AS ENUM (
  'BULK',
  'TIPPER',
  'PALLETIZED'
);

DROP TYPE IF EXISTS order_status CASCADE;
CREATE TYPE order_status AS ENUM (
  'ACCEPTED',
  'IN PROGRESS',
  'ARRIVED AT LOADING LOCATION',
  'LOADING',
  'ARRIVED AT UNLOADING LOCATION',
  'ON THE WAY',
  'UNLOADING',
  'COMPLETED'
);

DROP TYPE IF EXISTS contact_info_type CASCADE;
CREATE TYPE contact_info_type AS ENUM (
  'PHONE NUMBER',
  'TELEGRAM',
  'EMAIL'
);

CREATE TABLE IF NOT EXISTS person (
  person_id serial PRIMARY KEY,
  first_name varchar(20) NOT NULL,
  last_name varchar(20) NOT NULL,
  middle_name varchar(20),
  gender char(1) CHECK (gender IN ('M', 'F')) NOT NULL,
  date_of_birth date NOT NULL CHECK (date_of_birth >= '1910-01-01')
);

CREATE TABLE IF NOT EXISTS contact_info (
  person_id int REFERENCES person (person_id),
  contact_type contact_info_type,
  value text,
  PRIMARY KEY (person_id, contact_type)
);

CREATE TABLE IF NOT EXISTS driver (
  driver_id serial PRIMARY KEY,
  person_id int REFERENCES person(person_id) NOT NULL,
  passport varchar(10) NOT NULL CHECK (passport ~ '^[0-9]{10}$'),
  bank_card_number text NOT NULL
);

CREATE TABLE IF NOT EXISTS customer (
  customer_id serial PRIMARY KEY,
  person_id int REFERENCES person (person_id),
  organization varchar(50)
);

CREATE TABLE IF NOT EXISTS driver_status_history (
  driver_id int REFERENCES driver (driver_id),
  date date,
  status driver_status,
  PRIMARY KEY (driver_id, date)
);

CREATE TABLE IF NOT EXISTS tariff_rate (
  driver_id int REFERENCES driver (driver_id) PRIMARY KEY,
  daily_rate int NOT NULL,
  rate_per_km int NOT NULL
);

CREATE TABLE IF NOT EXISTS driver_license (
  driver_id int REFERENCES driver(driver_id) PRIMARY KEY,
  issue_date date NOT NULL,
  expiration_date date NOT NULL,
  license_number int,
  CONSTRAINT check_issue_expire_dates CHECK (issue_date < expiration_date)
);

CREATE TABLE IF NOT EXISTS vehicle (
  vehicle_id serial PRIMARY KEY,
  plate_number varchar(9) NOT NULL CHECK (
    plate_number ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{2}$' OR
    plate_number ~ '^[А-Я]{1}\d{3}[А-Я]{2}\d{3}$'
  ),
  model varchar(50) NOT NULL,
  manufacture_year date NOT NULL,
  length float NOT NULL,
  width float NOT NULL,
  height float NOT NULL,
  load_capacity float NOT NULL,
  body_type body_type
);

CREATE TABLE IF NOT EXISTS vehicle_ownership (
  vehicle_id int REFERENCES vehicle (vehicle_id),
  driver_id int REFERENCES driver (driver_id),
  ownership_start_date date,
  ownership_end_date date,
  PRIMARY KEY (vehicle_id, driver_id),
  CONSTRAINT check_ownership_dates CHECK (ownership_start_date <= ownership_end_date)
);

CREATE TABLE IF NOT EXISTS vehicle_movement_history (
  vehicle_id int REFERENCES vehicle (vehicle_id),
  date timestamp,
  latitude float NOT NULL,
  longitude float NOT NULL,
  mileage float NOT NULL,
  PRIMARY KEY (vehicle_id, date)
);

CREATE TABLE IF NOT EXISTS orders (
  order_id serial PRIMARY KEY,
  customer_id int NOT NULL REFERENCES customer (customer_id),
  distance float NOT NULL,
  price float NOT NULL,
  order_date date NOT NULL,
  vehicle_id int REFERENCES vehicle (vehicle_id)
);

CREATE TABLE IF NOT EXISTS order_statuses (
  order_id int REFERENCES orders (order_id),
  date_time timestamp,
  status order_status,
  PRIMARY KEY (order_id, date_time)
);

CREATE TABLE IF NOT EXISTS cargo (
  cargo_id serial PRIMARY KEY,
  weight float NOT NULL CHECK (weight <= 25000),
  width float NOT NULL CHECK (width <= 2.5),
  height float NOT NULL CHECK (height <= 4),
  length float NOT NULL CHECK (length <= 15),
  order_id int NOT NULL REFERENCES orders (order_id),
  cargo_type cargo_type
);

CREATE TABLE IF NOT EXISTS address (
  address_id serial PRIMARY KEY,
  country text NOT NULL,
  city text NOT NULL,
  street text NOT NULL,
  building int NOT NULL,
  corpus int
);

CREATE TABLE IF NOT EXISTS storage_point (
  address_id int REFERENCES address(address_id) PRIMARY KEY,
  longitude float NOT NULL CHECK (longitude >= -180 AND longitude <= 180),
  latitude float NOT NULL CHECK (latitude >= -90 AND latitude <= 90)
);

CREATE TABLE IF NOT EXISTS loading_unloading_agreement (
  order_id int REFERENCES orders (order_id) PRIMARY KEY,
  driver_id int NOT NULL REFERENCES driver (driver_id),
  departure_point int NOT NULL REFERENCES storage_point (address_id),
  delivery_point int NOT NULL REFERENCES storage_point (address_id),
  sender_id int NOT NULL REFERENCES person (person_id),
  receiver_id int NOT NULL REFERENCES person (person_id),
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
  driver_id int REFERENCES driver(driver_id),
  fuel_card_number varchar(40) NOT NULL,
  fuel_station_name varchar(50),
  PRIMARY KEY (driver_id, fuel_card_number),
  UNIQUE (fuel_card_number)
);

CREATE TABLE IF NOT EXISTS fuel_expenses (
  fuel_card_number varchar(40) REFERENCES fuel_cards_for_drivers(fuel_card_number) PRIMARY KEY,
  date date,
  amount double precision NOT NULL
);
