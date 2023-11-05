package org.ifmo.isbdcurs.models

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable

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

@Entity
data class Person(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val gender: Char,
    val dateOfBirth: LocalDate,
)

@Entity
data class ContactInfo(
    @Id val personId: Long,
    @Enumerated(EnumType.STRING)
    val contactType: ContactInfoType,
    val value: String,
)

@Entity
data class Driver(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val personId: Long,
    val passport: String,
    val bankCardNumber: String,
)

@Entity
data class Customer(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val personId: Long,
    val organization: String?,
)

data class DriverStatusHistoryPK(
    var driverId: Long? = null,
    var date: Instant? = null
) : java.io.Serializable

@Entity
@IdClass(DriverStatusHistoryPK::class)
data class DriverStatusHistory(
    @Id var driverId: Long,
    @Id val date: Instant,
    @Enumerated(EnumType.STRING)
    val status: DriverStatus,
)

@Entity
data class TariffRate(
    @Id var driverId: Long,
    val dailyRate: Int,
    val ratePerKm: Int,
)

@Entity
data class DriverLicense(
    @Id val driverId: Long,
    val issueDate: Instant,
    val expirationDate: Instant,
    val licenseNumber: Int,
)

@Entity
@Table(name = "vehicle")
data class Vehicle(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val plateNumber: String,
    val model: String,
    val manufactureYear: Instant,
    val length: Double,
    val width: Double,
    val height: Double,
    val loadCapacity: Double,
    @Enumerated(EnumType.STRING)
    val bodyType: BodyType,
)

data class VehicleOwnershipPK(
    var vehicleId: Long? = null,
    var driverId: Long? = null
) : java.io.Serializable

@Entity
@IdClass(VehicleOwnershipPK::class)
data class VehicleOwnership(
    @Id val vehicleId: Long,
    @Id val driverId: Long,
    val ownershipStartDate: Instant,
    val ownershipEndDate: Instant,
)

data class VehicleMovementHistoryPK(
    var vehicleId: Long? = null,
    var date: Instant? = null
) : java.io.Serializable

@Entity
@IdClass(VehicleMovementHistoryPK::class)
data class VehicleMovementHistory(
    @Id val vehicleId: Long,
    @Id val date: Instant,
    val latitude: Double,
    val longitude: Double,
    val mileage: Double,
)

@Entity
data class Orders(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val customerId: Long,
    val distance: Double,
    val price: Double,
    val orderDate: Instant,
    val vehicleId: Long?,
)

data class OrderStatusesPK(
    val orderId: Long,
    val dateTime: Instant
) : java.io.Serializable

@Entity
@IdClass(OrderStatusesPK::class)
data class OrderStatuses(
    @Id val orderId: Long,
    @Id val dateTime: Instant,
    @Enumerated(EnumType.STRING)
    val status: OrderStatus,
)

@Entity
data class Cargo(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val weight: Double,
    val width: Double,
    val height: Double,
    val length: Double,
    val orderId: Long,
    @Enumerated(EnumType.STRING)
    val cargoType: CargoType,
)

@Entity
data class Address(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id var id: Long? = null,
    val country: String,
    val city: String,
    val street: String,
    val building: Int,
    val corpus: Int?,
)

@Entity
data class StoragePoint(
    @Id val addressId: Long,
    val longitude: Double,
    val latitude: Double,
)

@Serializable
data class LoadingUnloadingAgreementPK(
    val orderId: Long? = null,
    val driverId: Long? = null
) : java.io.Serializable

@Entity
@IdClass(LoadingUnloadingAgreementPK::class)
data class LoadingUnloadingAgreement(
//    @EmbeddedId val id: LoadingUnloadingAgreementPK,
    @Id val orderId: Long,
    @Id val driverId: Long,
    val departurePoint: Long,
    val deliveryPoint: Long,
    val senderId: Long,
    val receiverId: Long,
    val unloadingTime: LocalTime,
    val loadingTime: LocalTime,
)

@Serializable
data class FuelCardsForDriversPK(
    val driverId: Long? = null,
    val fuelCardNumber: String? = null
) : java.io.Serializable

@Entity
@IdClass(FuelCardsForDriversPK::class)
data class FuelCardsForDrivers(
    @Id val driverId: Long,
    @Id val fuelCardNumber: String,
    val fuelStationName: String?,
)

data class FuelExpensesPK(
    var fuelCardNumber: String? = null,
    var date: Instant? = null
) : java.io.Serializable

@Entity
@IdClass(FuelExpensesPK::class)
data class FuelExpenses(
    @Id val fuelCardNumber: String,
    @Id val date: Instant,
    val amount: Double,
)