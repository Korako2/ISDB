package org.ifmo.isbdcurs.models

import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.util.Date

data class AddCustomerRequest(
    val firstName : String,
    val lastName : String,
    val gender : String,
    val dateOfBirth : Date,
    val middleName : String,
    val organization : String?,
)

data class AddDriverRequest(
    val firstName : String,
    val lastName : String,
    val middleName: String,
    val gender : String,
    val dateOfBirth : Date,
    val passport : String,
    val bankCardNumber : String,
)

data class AddDriverInfoRequest(
    val driverId : Long,
    val dailyRate : Int,
    val ratePerKm : Int,
    val issueDate : Date,
    val expirationDate : Date,
    val licenseNumber : Long,
    val fuelCard : String,
    val fuelStationName : String,
)


data class AddStoragePointRequest(
    val name : String,
    val country: String,
    val city: String,
    val street: String,
    val building: Int,
    val corpus: Int?,
    val latitude : Double,
    val longitude : Double,
)

data class UserDto(
    @NotEmpty(message = "Логин не может быть пустым")
    @Size(min = 6, max = 20, message = "Длина логина не менее 6 и не более 20 символов")
    val username: String,
    @NotEmpty(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 40, message = "Длина пароля не менее 6 и не более 40 символов")
    val password: String,
    @NotEmpty(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    val email: String,
    @NotEmpty(message = "Номер телефона не может быть пустым")
    @Pattern(regexp = "^[0-9]{11}$", message = "Неверный формат номера телефона")
    var phone: String,
)

data class StorageAddressRequest(
    @NotEmpty(message = "Поле не может быть пустым")
    val country: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Size(max = 50, message = "Длина города не более 50 символов")
    val city: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Size(max = 50, message = "Длина улицы не более 50 символов")
    val street: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @DecimalMin(value = "1", message = "Номер дома должен быть больше 0")
    @DecimalMax(value = "1000", message = "Номер дома должен быть меньше 1000")
    val building: Int
)

data class PhysicalParametersRequest(
    @DecimalMin(value = "0.1", message = "Длина должна быть не менее 0.1")
    @DecimalMax(value = "15", message = "Длина должна быть не более 15")
    val length: Double,
    @DecimalMin(value = "0.1", message = "Ширина должна быть не менее 0.1")
    @DecimalMax(value = "2.5", message = "Ширина должна быть не более 2.5")
    val width: Double,
    @DecimalMin(value = "0.1", message = "Высота должна быть не менее 0.1")
    @DecimalMax(value = "4", message = "Высота должна быть не более 4")
    val height: Double,
    @DecimalMin(value = "0.5", message = "Вес должен быть не менее 0.5")
    @DecimalMax(value = "25000", message = "Вес должен быть не более 25000")
    val weight: Double,
    @NotEmpty(message = "Поле не может быть пустым")
    val cargoType: String
)

data class TimeParametersRequest(
    @NotEmpty(message = "Поле не может быть пустым")
    @DateTimeFormat(pattern = "HH:mm")
    val loadingTime: Date,
    @DateTimeFormat(pattern = "HH:mm")
    val unloadingTime: Date,
)

data class OrderDataRequest(
    val departureStoragePoint: StorageAddressRequest,
    val deliveryStoragePoint: StorageAddressRequest,
    val orderParameters: PhysicalParametersRequest,
    val time: TimeParametersRequest
)
