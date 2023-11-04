package org.ifmo.isbdcurs.models

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
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
    @Id var id: Long? = null,
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
    @Id var id: Long? = null,
    val personId: Long,
    val bankCardNumber: String,
)

@Table
data class Customer(
    @Id var id: Long? = null,
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
    @Id var id: Long? = null,
    val plateNumber: String,
    val model: String,
    val manufactureYear: Instant,
    val length: Double,
    val width: Double,
    val height: Double,
    val loadCapacity: Double,
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
    @Id val date: Instant,
    val latitude: Double,
    val longitude: Double,
    val mileage: Double,
)

@Table
data class Order(
    @Id var id: Long? = null,
    val customerId: Long,
    val distance: Double,
    val price: Double,
    val orderDate: Instant,
    val vehicleId: Long?,
)

@Table
data class OrderStatuses(
    @Id val orderId: Long,
    @Id val dateTime: Instant,
    val status: OrderStatus,
)

@Table
data class Cargo(
    @Id var id: Long? = null,
    val weight: Double,
    val width: Double,
    val height: Double,
    val length: Double,
    val orderId: Long,
    val cargoType: CargoType,
)

@Table
data class Address(
    @Id var id: Long? = null,
    val country: String,
    val city: String,
    val street: String,
    val building: Int,
    val corpus: Int?,
)

@Table
data class StoragePoint(
    @Id val addressId: Long,
    val longitude: Double,
    val latitude: Double,
)

@Table
data class LoadingUnloadingAgreement(
    @Id val orderId: Long,
    val driverId: Long,
    val departurePoint: Long,
    val deliveryPoint: Long,
    val senderId: Long,
    val receiverId: Long,
    val unloadingTime: LocalTime,
    val loadingTime: LocalTime,
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