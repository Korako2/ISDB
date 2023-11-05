package org.ifmo.isbdcurs.models

import jakarta.persistence.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import org.ifmo.isbdcurs.persistence.InstantConverter
import org.ifmo.isbdcurs.persistence.LocalDateConverter
import org.ifmo.isbdcurs.persistence.LocalTimeConverter

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
    @Id var id: Long? = null,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val gender: Char,
    @Convert(converter = LocalDateConverter::class)
    val dateOfBirth: LocalDate,
)

@Entity
data class ContactInfo(
    @Id var personId: Long,
    val contactType: ContactInfoType,
    val value: String,
)

@Entity
data class Driver(
    @Id var id: Long? = null,
    val personId: Long,
    val passport: String,
    val bankCardNumber: String,
)

@Entity
data class Customer(
    @Id var id: Long? = null,
    val personId: Long,
    val organization: String?,
)

@Entity
data class DriverStatusHistory(
    @Id var driverId: Long,
    @Convert(converter = InstantConverter::class)
    val date: Instant,
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
    @Convert(converter = InstantConverter::class)
    val issueDate: Instant,
    @Convert(converter = InstantConverter::class)
    val expirationDate: Instant,
    val licenseNumber: Int,
)

@Entity
data class Vehicle(
    @Id var id: Long? = null,
    val plateNumber: String,
    val model: String,
    @Convert(converter = InstantConverter::class)
    val manufactureYear: Instant,
    val length: Double,
    val width: Double,
    val height: Double,
    val loadCapacity: Double,
    @Enumerated(EnumType.STRING)
    val bodyType: BodyType,
)

@Embeddable
data class VehicleOwnershipPK(
    val vehicleId: Long? = null,
    val driverId: Long? = null
) : java.io.Serializable

@Entity
@IdClass(VehicleOwnershipPK::class)
data class VehicleOwnership(
    @Id val vehicleId: Long,
    @Id val driverId: Long,
    @Convert(converter = InstantConverter::class)
    val ownershipStartDate: Instant,
    @Convert(converter = InstantConverter::class)
    val ownershipEndDate: Instant,
)

@Serializable
@Embeddable
data class VehicleMovementHistoryPK(
    val vehicleId: Long,
    val date: Instant
) : java.io.Serializable

@Entity
//@IdClass(VehicleMovementHistoryPK::class)
data class VehicleMovementHistory(
    @EmbeddedId
    val id: VehicleMovementHistoryPK,
//    @Id val vehicleId: Long,
//    @Id val date: Instant,
    val latitude: Double,
    val longitude: Double,
    val mileage: Double,
)

@Entity
data class Orders(
    @Id var id: Long? = null,
    val customerId: Long,
    val distance: Double,
    val price: Double,
    @Convert(converter = InstantConverter::class)
    val orderDate: Instant,
    val vehicleId: Long?,
)

@Serializable
data class OrderStatusesPK(
    val orderId: Long,
//    @Convert(converter = InstantConverter::class)
    val dateTime: Instant
) : java.io.Serializable

@Entity
data class OrderStatuses(
    @EmbeddedId
    val id: OrderStatusesPK,
//    @Id val orderId: Long,
//    @Convert(converter = InstantConverter::class)
//    @Id val dateTime: Instant,
    @Enumerated(EnumType.STRING)
    val status: OrderStatus,
)

@Entity
data class Cargo(
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
@Embeddable
data class LoadingUnloadingAgreementPK(
    val orderId: Long? = null,
    val driverId: Long? = null
) : java.io.Serializable

@Entity
//@IdClass(LoadingUnloadingAgreementPK::class)
data class LoadingUnloadingAgreement(
    @EmbeddedId val id: LoadingUnloadingAgreementPK,
//    @Id val orderId: Long,
//    @Id val driverId: Long,
    val departurePoint: Long,
    val deliveryPoint: Long,
    val senderId: Long,
    val receiverId: Long,
    @Convert(converter = LocalTimeConverter::class)
    val unloadingTime: LocalTime,
    @Convert(converter = LocalTimeConverter::class)
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

@Serializable
data class FuelExpensesPK(
    val fuelCardNumberId: Long? = null,
    @Convert(converter = InstantConverter::class)
    @Column(name = "date")
    val date: Instant? = null
) : java.io.Serializable

@Entity
@IdClass(FuelExpensesPK::class)
data class FuelExpenses(
    @Id val fuelCardNumberId: Long,
    @Convert(converter = InstantConverter::class)
    @Id val date: Instant,
    val amount: Double,
)