package org.ifmo.isbdcurs.models

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp
import java.time.Instant
import java.util.*

enum class DriverStatus {
    OFF_DUTY, ACCEPTED_ORDER, EN_ROUTE, ARRIVED_AT_LOADING_LOCATION, LOADING, ARRIVED_AT_UNLOADING_LOCATION, UNLOADING, COMPLETED_ORDER,
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

@Table
data class Person(
    @Id val id: Long? = null,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
)

@Table
data class ContactInfo(
    @Id val personId: Long,
    @Id val contactType: ContactInfoType,
    val value: String,
)

@Table
data class Driver(
    @Id val id: Long? = null,
    val personId: Long,
    val bankCardNumber: String,
)

@Table
data class Customer(
    @Id val id: Long? = null,
    val personId: Long,
    val organization: String?,
)

@Table
data class DriverStatusHistory(
    @Id val driverId: Long,
    @Id val date: Instant,
    val status: DriverStatus,
)

@Table
data class TariffRate(
    @Id val driverId: Long,
    val dailyRate: Int,
    val ratePerKm: Int,
)

@Table
data class DriverLicense(
    @Id val driverId: Long,
    val issueDate: Instant,
    val expirationDate: Instant,
    val licenseNumber: Int,
)

@Table
data class Vehicle(
    @Id val id: Long? = null,
    val plateNumber: String,
    val model: String,
    val manufactureYear: Instant,
    val length: Float,
    val width: Float,
    val height: Float,
    val loadCapacity: Float,
    val bodyType: BodyType,
)

@Table
data class VehicleOwnership(
    @Id val vehicleId: Long,
    @Id val driverId: Long,
    val ownershipStartDate: Instant,
    val ownershipEndDate: Instant,
)

@Table
data class VehicleMovementHistory(
    @Id val vehicleId: Long,
    @Id val date: Date,
    val latitude: Float,
    val longitude: Float,
    val mileage: Float,
)

@Table
data class Order(
    @Id val id: Long? = null,
    val customerId: Long,
    val distance: Float,
    val price: Float,
    val orderDate: Date,
    val vehicleId: Long?,
)

@Table
data class OrderStatuses(
    @Id val orderId: Long,
    @Id val dateTime: Timestamp,
    val status: OrderStatus,
)

@Table
data class Cargo(
    @Id val id: Long? = null,
    val weight: Float,
    val width: Float,
    val height: Float,
    val length: Float,
    val orderId: Long,
    val cargoType: CargoType,
)

@Table
data class Address(
    @Id val id: Long? = null,
    val country: String,
    val city: String,
    val street: String,
    val building: Int,
    val corpus: Int?,
)

@Table
data class StoragePoint(
    @Id val addressId: Long,
    val longitude: Float,
    val latitude: Float,
)

@Table
data class LoadingUnloadingAgreement(
    @Id val orderId: Long,
    val driverId: Long,
    val departurePoint: Long,
    val deliveryPoint: Long,
    val senderId: Long,
    val receiverId: Long,
    val unloadingTime: Timestamp,
    val loadingTime: Timestamp,
)

@Table
data class FuelCardsForDrivers(
    @Id val driverId: Long,
    @Id val fuelCardNumber: String,
    val fuelStationName: String?,
)

@Table
data class FuelExpenses(
    @Id val fuelCardNumberId: Long,
    val date: Date,
    val amount: Double,
)