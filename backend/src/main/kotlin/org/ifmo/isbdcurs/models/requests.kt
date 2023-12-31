package org.ifmo.isbdcurs.models

import jakarta.validation.constraints.*
import org.springframework.format.annotation.DateTimeFormat
import java.util.Date

data class AddCustomerRequest(
    val firstName : String,
    val lastName : String,
    val gender : String,
    val dateOfBirth : Date,
    val middleName : String?,
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

data class StorageAddressDto(
    val country: String,
    val city: String,
    val street: String,
    val building: Int
)

data class OrderDataRequest(
    @NotEmpty(message = "Поле не может быть пустым")
    val departureCountry: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Size(max = 50, message = "Длина города не более 50 символов")
    val departureCity: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Size(max = 50, message = "Длина улицы не более 50 символов")
    val departureStreet: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @DecimalMin(value = "1", message = "Номер дома должен быть больше 0")
    @DecimalMax(value = "1000", message = "Номер дома должен быть меньше 1000")
    val departureHouse: Int,
    @NotEmpty(message = "Поле не может быть пустым")
    val destinationCountry: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Size(max = 50, message = "Длина города не более 50 символов")
    val destinationCity: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Size(max = 50, message = "Длина улицы не более 50 символов")
    val destinationStreet: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @DecimalMin(value = "1", message = "Номер дома должен быть больше 0")
    @DecimalMax(value = "1000", message = "Номер дома должен быть меньше 1000")
    val destinationHouse: Int,
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
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Неверный формат времени")
    val loadingTime: String,
    @NotEmpty(message = "Поле не может быть пустым")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Неверный формат времени")
    val unloadingTime: String,
    @NotEmpty(message = "Поле не может быть пустым")
    val cargoType: String
)

data class AddNewCustomer (
    @field:Size(max = 40, message = "Фамилия не должна превышать 40 символов")
    var firstName: String,
    @field:Size(max = 40, message = "Фамилия не должна превышать 40 символов")
    var lastName: String,
    @field:Pattern(regexp = "\\b(?:M|F)\\b", message = "Гендер должен быть 'M' или 'F'")
    var gender: String,
    @field:DateTimeFormat(pattern = "yyyy-MM-dd")
    @field:NotNull(message = "Дата рождения не может быть пустой")
    var dateOfBirth: Date?,
    @field:Size(max = 40, message = "Логин не должен превышать 40 символов")
    var username: String,
    var password: String,
    @field:Email(message = "Некорректный формат email")
    var email: String,
    @field:Pattern(regexp = "\\d{11}", message = "Телефонный номер должен содержать 11 цифр")
    @field:NotNull(message = "Телефонный номер не может быть пустым")
    var phone: String,
    var isAdmin: Boolean = false
)

data class DriverRequest (
    val firstName : String,
    val lastName : String,
    val middleName: String,
    val gender : String,
    val dateOfBirth : String,
    val passport : String,
    val bankCardNumber : String,
    val dailyRate : Int,
    val ratePerKm : Int,
    val issueDate : String,
    val expirationDate : String,
    val licenseNumber : String,
    val fuelCard : String,
    val fuelStationName : String,
    val phone : String,
    val email : String,
)
