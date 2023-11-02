package org.ifmo.isbdcurs.models

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Max
import org.hibernate.annotations.Check
import org.hibernate.validator.constraints.Range
import java.sql.Date
import java.sql.Timestamp

enum class DriverStatus {
    ACCEPTED_ORDER, OFF_DUTY, EN_ROUTE, ARRIVED_AT_LOADING_LOCATION, LOADING, ARRIVED_AT_UNLOADING_LOCATION, UNLOADING, COMPLETED_ORDER,
}

enum class BodyType {
    OPEN, CLOSED,
}

enum class CargoType {
    BULK, TIPPER, PALLETIZED,
}

enum class OrderStatus {
    ACCEPTED, IN_PROGRESS, ARRIVED_AT_LOADING_LOCATION, LOADING, ARRIVED_AT_UNLOADING_LOCATION, ON_THE_WAY, UNLOADING, COMPLETED,
}

enum class ContactInfoType {
    PHONE_NUMBER, TELEGRAM, EMAIL,
}

@Entity
open class Person(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE) open val id: Long? = null,
    open val firstName: String,
    open val lastName: String,
    open val middleName: String?,
    @Check(constraints = "gender IN ('M', 'F')") open val gender: Char,
    @Check(constraints = "date_of_birth >= '1910-01-01'") open val dateOfBirth: Date,
)

@Entity
open class ContactInfo(
    @Id open val personId: Long,
    @Id open val contactType: ContactInfoType,
    open val value: String,
)

@Entity
open class Driver(
    @Id open val id: Long? = null,
    open val personId: Long,
    @Check(constraints = "passport ~ '^[0-9]{10}$'") open val passport: String,
    open val bankCardNumber: String,
)

@Entity
open class Customer(
    @Id open val id: Long? = null,
    open val personId: Long,
    open val organization: String?,
)

/*
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
 */

@Entity
open class DriverStatusHistory(
    @Id open val driverId: Long,
    @Id open val date: Date,
    open val status: DriverStatus,
)

@Entity
open class TariffRate(
    @Id open val driverId: Long,
    open val dailyRate: Int,
    open val ratePerKm: Int,
)

@Entity
@Check(constraints = "issueDate < expirationDate")
open class DriverLicense(
    @Id open val driverId: Long,
    open val issueDate: Date,
    open val expirationDate: Date,
    open val licenseNumber: Int,
)

@Entity
open class Vehicle(
    @Id open val id: Long? = null,
    @Check(constraints = "plateNumber ~ '^[А-Я]{1}\\d{3}[А-Я]{2}\\d{2}$' OR plateNumber ~ '^[А-Я]{1}\\d{3}[А-Я]{2}\\d{3}$'")
    open val plateNumber: String,
    open val model: String,
    open val manufactureYear: Date,
    open val length: Float,
    open val width: Float,
    open val height: Float,
    open val loadCapacity: Float,
    open val bodyType: BodyType,
)

@Entity
@Check(constraints = "ownershipStartDate <= ownershipEndDate")
open class VehicleOwnership(
    @Id open val vehicleId: Long,
    @Id open val driverId: Long,
    open val ownershipStartDate: Date,
    open val ownershipEndDate: Date,
)

@Entity
open class VehicleMovementHistory(
    @Id open val vehicleId: Long,
    @Id open val date: Date,
    open val latitude: Float,
    open val longitude: Float,
    open val mileage: Float,
)

@Entity
open class Order(
    @Id open val id: Long? = null,
    open val customerId: Long,
    open val distance: Float,
    open val price: Float,
    open val orderDate: Date,
    open val vehicleId: Long?,
)

@Entity
open class OrderStatuses(
    @Id open val orderId: Long,
    @Id open val dateTime: Timestamp,
    open val status: OrderStatus,
)

@Entity
open class Cargo(
    @Id open val id: Long? = null,
    @Max(25000)
    open val weight: Float,
    @DecimalMax("2.5")
    open val width: Float,
    @Max(4)
    open val height: Float,
    @Max(15)
    open val length: Float,
    open val orderId: Long,
    open val cargoType: CargoType,
)

@Entity
open class Address(
    @Id open val id: Long? = null,
    open val country: String,
    open val city: String,
    open val street: String,
    open val building: Int,
    open val corpus: Int?,
)

@Entity
open class StoragePoint(
    @Id open val addressId: Long,
    @Range(min=-180, max=180)
    open val longitude: Float,
    @Range(min=-90, max=90)
    open val latitude: Float,
)

@Entity
@Check(constraints = "departurePoint <> deliveryPoint")
open class LoadingUnloadingAgreement(
    @Id open val orderId: Long,
    open val driverId: Long,
    open val departurePoint: Long,
    open val deliveryPoint: Long,
    open val senderId: Long,
    open val receiverId: Long,
    @Check(constraints = "unloadingTime >= '00:01'::time AND unloadingTime < '24:00'::time")
    open val unloadingTime: Timestamp,
    @Check(constraints = "loadingTime >= '00:01'::time AND loadingTime < '24:00'::time")
    open val loadingTime: Timestamp,
)

@Entity
open class FuelCardsForDrivers(
    @Id open val driverId: Long,
    @Id open val fuelCardNumber: String,
    open val fuelStationName: String?,
)

@Entity
open class FuelExpenses(
    @Id open val fuelCardNumberId: Long,
    open val date: Date,
    open val amount: Double,
)