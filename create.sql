-- Creating the "DRIVER STATUS" enumeration
CREATE TYPE DRIVER_STATUS AS ENUM (
  'OFF DUTY',
  'EN ROUTE',
  'UNLOADING',
  'LOADING',
  'WAITING FOR ORDER',
  'WAITING FOR LOADING',
  'WAITING FOR UNLOADING',
  'ARRIVED AT LOADING LOCATION'
);

-- Creating the "BODY TYPE" enumeration
CREATE TYPE BODY_TYPE AS ENUM (
  'OPEN',
  'CLOSED'
);

CREATE TYPE CARGO_TYPE AS ENUM (
  'BULK',
  'TIPPER',
  'PALLETIZED'
);

-- Creating the "ORDER STATUS" enumeration
CREATE TYPE ORDER_STATUS AS ENUM (
  'ACCEPTED',
  'IN PROGRESS',
  'ARRIVED AT LOADING LOCATION',
  'LOADING',
  'ARRIVED AT UNLOADING LOCATION',
  'ON THE WAY',
  'DELIVERED',
  'COMPLETED'
);

-- Creating the "CONTACT INFORMATION TYPE" enumeration
CREATE TYPE CONTACT_INFO_TYPE AS ENUM (
  'PHONE NUMBER',
  'TELEGRAM',
  'EMAIL'
);

-- Creating the "PERSON" table
CREATE TABLE IF NOT EXISTS PERSON (
  PERSON_ID serial PRIMARY KEY,
  FIRST_NAME VARCHAR(20) NOT NULL,
  LAST_NAME VARCHAR(20) NOT NULL,
  MIDDLE_NAME VARCHAR(20),
  GENDER CHAR(1) NOT NULL,
  PASSPORT VARCHAR(10)
);

-- Creating the "CONTACT INFO" table
CREATE TABLE IF NOT EXISTS CONTACT_INFO (
  PERSON_ID int REFERENCES PERSON (PERSON_ID),
  CONTACT_TYPE CONTACT_INFO_TYPE,
  VALUE text,
  PRIMARY KEY (PERSON_ID, CONTACT_TYPE)
);

-- Creating the "DRIVER" table
CREATE TABLE IF NOT EXISTS DRIVER (
  DRIVER_ID serial PRIMARY KEY,
  CONTACT_INFO VARCHAR(11) NOT NULL,
  PASSPORT VARCHAR(10) NOT NULL,
  BANK_CARD_NUMBER text NOT NULL
);

-- Creating the "CUSTOMER" table
CREATE TABLE IF NOT EXISTS CUSTOMER (
  CUSTOMER_ID serial PRIMARY KEY,
  PERSON_ID int REFERENCES PERSON (PERSON_ID),
  ORGANIZATION VARCHAR(50)
);

-- Creating the "DRIVER STATUS HISTORY" table
CREATE TABLE IF NOT EXISTS DRIVER_STATUS_HISTORY (
  DRIVER_ID int REFERENCES DRIVER (DRIVER_ID),
  DATE date,
  STATUS DRIVER_STATUS,
  PRIMARY KEY (DRIVER_ID, DATE)
);

-- Creating the "TARIFF RATE" table
CREATE TABLE IF NOT EXISTS TARIFF_RATE (
  DRIVER_ID int REFERENCES DRIVER (DRIVER_ID) PRIMARY KEY,
  DAILY_RATE int NOT NULL,
  RATE_PER_KM int NOT NULL
);

-- Creating the "DRIVER LICENSE" table
CREATE TABLE IF NOT EXISTS DRIVER_LICENSE (
  DRIVER_ID int REFERENCES DRIVER(DRIVER_ID) PRIMARY KEY,
  ISSUE_DATE date NOT NULL,
  EXPIRATION_DATE date NOT NULL,
  LICENSE_NUMBER int
);

-- Creating the "VEHICLE" table
CREATE TABLE IF NOT EXISTS VEHICLE (
  VEHICLE_ID serial PRIMARY KEY,
  PLATE_NUMBER varchar(9) NOT NULL,
  MODEL varchar(50) NOT NULL,
  MANUFACTURE_YEAR date NOT NULL,
  LENGTH float NOT NULL,
  WIDTH float NOT NULL,
  HEIGHT float NOT NULL,
  LOAD_CAPACITY float NOT NULL,
  BODY_TYPE BODY_TYPE
);

-- Creating the "VEHICLE OWNERSHIP" table
CREATE TABLE IF NOT EXISTS VEHICLE_OWNERSHIP (
  VEHICLE_ID int REFERENCES VEHICLE (VEHICLE_ID),
  DRIVER_ID int REFERENCES DRIVER (DRIVER_ID),
  OWNERSHIP_START_DATE date,
  OWNERSHIP_END_DATE date,
  PRIMARY KEY (VEHICLE_ID, DRIVER_ID)
);

-- Creating the "VEHICLE MOVEMENT HISTORY" table
CREATE TABLE IF NOT EXISTS VEHICLE_MOVEMENT_HISTORY (
  VEHICLE_ID int REFERENCES VEHICLE (VEHICLE_ID),
  DATE timestamp,
  LATITUDE float NOT NULL,
  LONGITUDE float NOT NULL,
  MILEAGE float NOT NULL,
  PRIMARY KEY (VEHICLE_ID, DATE)
);

-- Creating the "ORDER" table
CREATE TABLE IF NOT EXISTS ORDERS (
  ORDER_ID serial PRIMARY KEY,
  CUSTOMER_ID int NOT NULL REFERENCES CUSTOMER (CUSTOMER_ID),
  DISTANCE float NOT NULL,
  PRICE float NOT NULL,
  ORDER_DATE date NOT NULL,
  VEHICLE_ID int REFERENCES VEHICLE (VEHICLE_ID)
);

-- Creating the "ORDER STATUSES" table
CREATE TABLE IF NOT EXISTS ORDER_STATUSES (
  ORDER_ID int REFERENCES ORDERS (ORDER_ID),
  TIME timestamp,
  STATUS ORDER_STATUS,
  PRIMARY KEY (ORDER_ID, TIME)
);

-- Creating the "CARGO" table
CREATE TABLE IF NOT EXISTS CARGO (
  CARGO_ID serial PRIMARY KEY,
  WEIGHT float NOT NULL,
  WIDTH float NOT NULL,
  HEIGHT float NOT NULL,
  LENGTH float NOT NULL,
  ORDER_ID int NOT NULL REFERENCES ORDERS (ORDER_ID),
  CARGO_TYPE CARGO_TYPE
);

-- Creating the "ADDRESS" table
CREATE TABLE IF NOT EXISTS ADDRESS (
  ADDRESS_ID serial PRIMARY KEY,
  COUNTRY text NOT NULL,
  CITY text NOT NULL,
  STREET text NOT NULL,
  BUILDING int NOT NULL,
  CORPUS int
);

-- Creating the "STORAGE POINT" table
CREATE TABLE IF NOT EXISTS STORAGE_POINT (
  ADDRESS_ID int REFERENCES ADDRESS(ADDRESS_ID) PRIMARY KEY,
  LONGITUDE float NOT NULL,
  LATITUDE float NOT NULL
);

-- Creating the "LOADING_UNLOADING AGREEMENT" table
CREATE TABLE IF NOT EXISTS LOADING_UNLOADING_AGREEMENT (
  ORDER_ID int REFERENCES ORDERS (ORDER_ID) PRIMARY KEY,
  DRIVER_ID int NOT NULL REFERENCES DRIVER (DRIVER_ID),
  DEPARTURE_POINT int NOT NULL REFERENCES STORAGE_POINT (ADDRESS_ID),
  DELIVERY_POINT int NOT NULL REFERENCES STORAGE_POINT (ADDRESS_ID),
  SENDER_ID int NOT NULL REFERENCES PERSON (PERSON_ID),
  RECEIVER_ID int NOT NULL REFERENCES PERSON (PERSON_ID),
  UNLOADING_TIME time NOT NULL,
  LOADING_TIME time NOT NULL
);

-- Creating the "FUEL CARDS FOR DRIVERS" table
CREATE TABLE IF NOT EXISTS FUEL_CARDS_FOR_DRIVERS (
  DRIVER_ID int REFERENCES DRIVER(DRIVER_ID),
  FUEL_CARD_NUMBER VARCHAR(40) NOT NULL,
  FUEL_STATION_NAME VARCHAR(50),
  PRIMARY KEY (DRIVER_ID, FUEL_CARD_NUMBER),
  UNIQUE (FUEL_CARD_NUMBER)
);

-- Creating the "FUEL EXPENSES" table
CREATE TABLE IF NOT EXISTS FUEL_EXPENSES (
  FUEL_CARD_NUMBER VARCHAR(40) REFERENCES FUEL_CARDS_FOR_DRIVERS(FUEL_CARD_NUMBER) PRIMARY KEY,
  DATE date,
  AMOUNT double precision NOT NULL
);

