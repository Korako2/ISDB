package org.ifmo.isbdcurs.models

import jakarta.validation.constraints.*
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

data class AddOrderRequest(
    val distance : Int,
    val weight : Int,
    val width : Int,
    val height : Int,
    val length : Int,
    val cargoType : String,
    val latitude : Double,
    val longitude : Double,
    val departurePointId : Long,
    val deliveryPointId : Long,
    val senderId : Long,
    val receiverId : Long,
    val unloadingTime : Long = 3,
    val loadingTime : Long = 3,
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

data class OrderData(
    @NotEmpty(message = "Поле не может быть пустым")
    val departure: String,
    @NotEmpty(message = "Поле не может быть пустым")
    val destination: String,
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
    val loadingTime: String,
    @NotEmpty(message = "Поле не может быть пустым")
    val unloadingTime: String,
    @NotEmpty(message = "Поле не может быть пустым")
    val cargoType: String
)
