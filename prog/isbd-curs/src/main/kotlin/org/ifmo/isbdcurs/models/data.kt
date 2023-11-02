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