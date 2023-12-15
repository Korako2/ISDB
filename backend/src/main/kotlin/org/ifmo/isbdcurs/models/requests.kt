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