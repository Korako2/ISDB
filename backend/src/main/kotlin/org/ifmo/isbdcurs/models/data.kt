package org.ifmo.isbdcurs.models

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable
import java.util.*
import jakarta.validation.constraints.*

enum class DriverStatus {
    OFF_DUTY, ACCEPTED_ORDER, ARRIVED_AT_LOADING_LOCATION, LOADING, EN_ROUTE, ARRIVED_AT_UNLOADING_LOCATION, UNLOADING, COMPLETED_ORDER, WEEKEND, READY_FOR_NEW_ORDER
}

enum class BodyType {
    OPEN, CLOSED,
}

enum class CargoType(val translatedName: String) {
    BULK("Сыпучие"),
    TIPPER("Самосвал"),
    PALLETIZED("Паллеты");
}

enum class OrderStatus {
    WAITING, ACCEPTED, ARRIVED_AT_LOADING_LOCATION, LOADING, ON_THE_WAY, ARRIVED_AT_UNLOADING_LOCATION, UNLOADING, COMPLETED;
}

fun OrderStatus.translate(): String {
    return when (this) {
        OrderStatus.WAITING -> "Ожидание"
        OrderStatus.ACCEPTED -> "Принят"
        OrderStatus.ARRIVED_AT_LOADING_LOCATION -> "Прибыл на погрузку"
        OrderStatus.LOADING -> "Погрузка"
        OrderStatus.ON_THE_WAY -> "В пути"
        OrderStatus.ARRIVED_AT_UNLOADING_LOCATION -> "Прибыл на разгрузку"
        OrderStatus.UNLOADING -> "Разгрузка"
        OrderStatus.COMPLETED -> "Завершен"
    }
}

fun DriverStatus.translate(): String {
    return when (this) {
        DriverStatus.OFF_DUTY -> "Не работает"
        DriverStatus.ACCEPTED_ORDER -> "Принял заказ"
        DriverStatus.ARRIVED_AT_LOADING_LOCATION -> "Прибыл на погрузку"
        DriverStatus.LOADING -> "Погрузка"
        DriverStatus.EN_ROUTE -> "В пути"
        DriverStatus.ARRIVED_AT_UNLOADING_LOCATION -> "Прибыл на разгрузку"
        DriverStatus.UNLOADING -> "Разгрузка"
        DriverStatus.COMPLETED_ORDER -> "Завершил заказ"
        DriverStatus.WEEKEND -> "Выходной"
        DriverStatus.READY_FOR_NEW_ORDER -> "Готов к новому заказу"
    }
}

fun valueFrom(value: String): CargoType {
    return when (value) {
        "Сыпучие" -> CargoType.BULK
        "Самосвал" -> CargoType.TIPPER
        "Паллеты" -> CargoType.PALLETIZED
        else -> CargoType.BULK
    }
}

enum class ContactInfoType {
    PHONE_NUMBER, TELEGRAM, EMAIL;

    override fun toString(): String {
        return when (this) {
            PHONE_NUMBER -> "PHONE NUMBER"
            TELEGRAM -> "TELEGRAM"
            EMAIL -> "EMAIL"
        }
    }
}

enum class Gender {
    M, F,
}

@Entity
data class Person(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_id_seq")
    @SequenceGenerator(name = "person_id_seq", sequenceName = "person_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    @Enumerated(EnumType.STRING)
    val gender: Gender,
    val dateOfBirth: Instant,
)

data class ContactInfoPK(
    var personId: Long? = null,
    var contactType: String? = null,
) : java.io.Serializable

@Entity
@IdClass(ContactInfoPK::class)
data class ContactInfo(
    @Id val personId: Long,
    @Id val contactType: String,
    val value: String,
)

@Entity
@NamedStoredProcedureQueries(
    NamedStoredProcedureQuery(
        name = "addDriverInfo", procedureName = "add_driver_info",
        parameters = arrayOf(
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_driver_id", type = Int::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_daily_rate", type = Int::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_rate_per_km", type = Int::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_issue_date", type = Date::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_expiration_date", type = Date::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_license_number", type = Int::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_fuel_card", type = String::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_fuel_station_name", type = String::class),
        )
    )
)
data class Driver(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "driver_gen")
    @SequenceGenerator(name = "driver_gen", sequenceName = "driver_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val personId: Long,
    val passport: String,
    val bankCardNumber: String,
)

@Entity
data class Customer(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_gen")
    @SequenceGenerator(name = "customer_gen", sequenceName = "customer_id_seq", allocationSize = 1)
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_id_seq")
    @SequenceGenerator(name = "vehicle_id_seq", sequenceName = "vehicle_id_seq", allocationSize = 1)
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
    val ownershipEndDate: Instant?,
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
    val latitude: Float,
    val longitude: Float,
    val mileage: Float,
)

@Entity
@NamedStoredProcedureQueries(
    NamedStoredProcedureQuery(
        name = "addOrder", procedureName = "add_order",
        parameters = arrayOf(
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_customer_id", type = Long::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_distance", type = Float::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_vehicle_id", type = Long::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_weight", type = Float::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_width", type = Float::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_height", type = Float::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_length", type = Float::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_cargo_type", type = String::class),
            StoredProcedureParameter(mode = ParameterMode.IN, name = "v_date", type = Date::class),
        )
    )
)

@Table(name = "orders")
data class Order(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_id_seq")
    @SequenceGenerator(name = "orders_id_seq", sequenceName = "orders_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val customerId: Long,
    val distance: Float,
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cargo_id_seq")
    @SequenceGenerator(name = "cargo_id_seq", sequenceName = "cargo_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val weight: Float,
    val width: Float,
    val height: Float,
    val length: Float,
    val orderId: Long,
    @Enumerated(EnumType.STRING)
    val cargoType: CargoType,
)

@Entity
data class Address(
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_id_seq")
    @SequenceGenerator(name = "address_id_seq", sequenceName = "address_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val country: String,
    val city: String,
    val street: String,
    val building: Int,
    val corpus: Int?,
) {
    override fun toString(): String {
        return "$country, $city, $street, $building"
    }
}

@Entity
data class StoragePoint(
    @Id val addressId: Long,
    val latitude: Float,
    val longitude: Float,
)

@Entity
data class LoadingUnloadingAgreement(
    @Id val orderId: Long,
    val driverId: Long?,
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
    val amount: Float,
)

data class ExtendedOrder (
    val id: Long,
    val customerName: String,
    val driverName: String?,
    val departurePoint: Long,
    val deliveryPoint: Long,
    val status: OrderStatus,
)


data class CustomerOrder (
    val id: Long,
    val statusChangedTime: Instant,
    val driverName: String?,
    val departureAddress: Address,
    val deliveryAddress: Address,
    val status: OrderStatus,
 )

data class ManagerOrder (
    val id: Long,
    val statusChangedTime: Instant,
    val value: String,
    val departureAddress: Address,
    val deliveryAddress: Address,
    val status: OrderStatus
)

data class FullOrdersInfo (
    val id: Long,
    val statusChangedTime: Instant,
    val value: String,
    val customerFirstName: String,
    val customerLastName: String,
    val cargo: Cargo,
    val loadingTime: LocalTime,
    val unloadingTime: LocalTime,
    val departureAddress: Address,
    val deliveryAddress: Address,
    val status: OrderStatus
)

data class SuitableDriver (
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val status: String,
    val licenseNumber: Int,
    val issueDate: String,
    val expirationDate: String,
    val vehicle: Vehicle
)


@Entity
@Table(name = "logs")
data class AdminLogRow (
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logs_id_seq")
    @SequenceGenerator(name = "logs_id_seq", sequenceName = "logs_id_seq", allocationSize = 1)
    @Id var id: Long? = null,
    val message: String,
    @Enumerated(EnumType.STRING)
    val level: LogLevels,
    val timestamp: Instant,
)
