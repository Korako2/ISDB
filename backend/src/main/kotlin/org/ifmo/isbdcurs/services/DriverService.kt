package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.DriverRepository
import org.ifmo.isbdcurs.persistence.FuelCardsForDriversRepository
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.ifmo.isbdcurs.util.pageToIdRangeNormal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.BindingResult
import java.time.LocalDate

@Service
class DriverService @Autowired constructor(
    private val driverRepository: DriverRepository,
    private val fuelCardsForDriversRepository: FuelCardsForDriversRepository,
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverService::class.java)
    private val exceptionHelper = ExceptionHelper(logger)

    fun addDriver(addDriverRequest: AddDriverRequest) : Long {
        return driverRepository.addDriver(addDriverRequest)
    }

    fun addDriverInfo(addDriverInfoRequest: AddDriverInfoRequest) {
        driverRepository.addDriverInfo(
            addDriverInfoRequest.driverId.toInt(),
            addDriverInfoRequest.dailyRate,
            addDriverInfoRequest.ratePerKm,
            addDriverInfoRequest.issueDate,
            addDriverInfoRequest.expirationDate,
            addDriverInfoRequest.licenseNumber.toInt(),
            addDriverInfoRequest.fuelCard,
            addDriverInfoRequest.fuelStationName,
        )
    }

    fun getTotalPages(pageSize: Int): Long {
        return (driverRepository.count() + pageSize - 1) / pageSize
    }

    fun getDriversPaged(page: Int, size: Int): List<DriverResponse> {
        val (minOrderId, maxOrderId) = pageToIdRangeNormal(page, size)
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            driverRepository.getExtendedDriversPaged(minOrderId, maxOrderId)
        }
    }

    fun startWork(driverId: Long, orderId: Long) {
        // slowly move to order address
    }

    fun isValidData(driverRequest: DriverRequest, result: BindingResult): Boolean{
        var isValid = true
        isValid = isValidName(driverRequest, result) && isValid
        isValid = isValidGender(driverRequest, result) && isValid
        isValid = isValidDate(driverRequest.dateOfBirth, "dateOfBirth", "Неверный формат даты рождения", true, result) && isValid
        isValid = isDateGreaterThan(driverRequest.dateOfBirth, "1924-01-01", "dateOfBirth", "Водитель должен быть младше 100 лет", result) && isValid
        isValid = isValidDate(driverRequest.issueDate, "issueDate", "Неверный формат даты получения ВУ", false, result) && isValid
        isValid = isValidDate(driverRequest.expirationDate, "expirationDate", "Неверный формат даты истечения ВУ", false, result) && isValid
        isValid = isDateInFuture(driverRequest.expirationDate, "expirationDate", "срок действия ВУ истек", result) && isValid
        isValid = isValidPassport(driverRequest, result) && isValid
       // isValid = isValidDriverLicense(driverRequest, result) && isValid
        isValid = isValidDailyRate(driverRequest, result) && isValid
        isValid = isValidRatePerKm(driverRequest, result) && isValid
        isValid = isValidFuelCard(driverRequest, result) && isValid
        isValid = isValidFuelStationName(driverRequest, result) && isValid
        return isValid
    }

    private fun isDateInFuture(date: String, fieldName: String, errorMessage: String, result: BindingResult): Boolean {
        val dateParts = date.split("-")
        val day = dateParts[2].toIntOrNull()
        val month = dateParts[1].toIntOrNull()
        val year = dateParts[0].toIntOrNull()
        if (day == null || month == null || year == null) {
            return false
        }
        val isInFuture = LocalDate.of(year, month, day).isAfter(LocalDate.now())
        if (!isInFuture) {
            result.rejectValue(fieldName, fieldName, errorMessage)
            return false
        }
        return true
    }

    private fun isDateGreaterThan(date1: String, date2: String, fieldName: String, errorMessage: String, result: BindingResult): Boolean {
        val dateParts1 = date1.split("-")
        val day1 = dateParts1[2].toIntOrNull()
        val month1 = dateParts1[1].toIntOrNull()
        val year1 = dateParts1[0].toIntOrNull()
        val dateParts2 = date2.split("-")
        val day2 = dateParts2[2].toIntOrNull()
        val month2 = dateParts2[1].toIntOrNull()
        val year2 = dateParts2[0].toIntOrNull()
        if (day1 == null || month1 == null || year1 == null || day2 == null || month2 == null || year2 == null) {
            result.rejectValue(fieldName, fieldName, "Неверный формат даты")
            return false
        }
        if (LocalDate.of(year1, month1, day1).isBefore(LocalDate.of(year2, month2, day2))) {
            result.rejectValue(fieldName, fieldName, errorMessage)
            return false
        }
        return true
    }

    private fun isValidName(driverRequest: DriverRequest, result: BindingResult): Boolean {
        var isValid = true
        if (driverRequest.firstName.isBlank()) {
            result.rejectValue("firstName", "firstName", "Имя не может быть пустым")
            isValid = false
        }
        if (driverRequest.lastName.isBlank()) {
            result.rejectValue("lastName", "lastName", "Фамилия не может быть пустой")
            isValid = false
        }
        if (driverRequest.firstName.length >= 40) {
            result.rejectValue("firstName", "firstName", "Имя не может быть длиннее 40 символов")
            isValid = false
        }
        if (driverRequest.lastName.length >= 40) {
            result.rejectValue("lastName", "lastName", "Фамилия не может быть длиннее 40 символов")
            isValid = false
        }
        if (driverRequest.middleName.length >= 40) {
            result.rejectValue("middleName", "middleName", "Отчество не может быть длиннее 40 символов")
            isValid = false
        }
        return isValid
    }

    private fun isValidGender(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.gender !in arrayOf("M", "F")) {
            result.rejectValue("gender", "gender", "Пол должен быть M или F")
            return false
        }
        return true
    }

    private fun isValidPassport(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.passport.isBlank()) {
            result.rejectValue("passport", "passport", "Номер паспорта не может быть пустым")
            return false
        }
        if (driverRequest.passport.length != 10) {
            result.rejectValue("passport", "passport", "Номер паспорта должен содержать 10 цифр")
            return false
        }
        if (driverRepository.existsByPassport(driverRequest.passport)) {
            result.rejectValue("passport", "passport", "Водитель с таким паспортом уже существует")
            return false
        }
        return true
    }

    private fun isValidDriverLicense(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.licenseNumber.isBlank()) {
            result.rejectValue("driverLicense", "driverLicense", "Номер водительского удостоверения не может быть пустым")
            return false
        }
        if (driverRequest.licenseNumber.length != 10) {
            result.rejectValue("driverLicense", "driverLicense", "Номер водительского удостоверения должен содержать 10 цифр")
            return false
        }
        return true
    }

    private fun isValidDailyRate(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.dailyRate < 0) {
            result.rejectValue("dailyRate", "dailyRate", "Ставка за день не может быть отрицательной")
            return false
        }
        return true
    }

    private fun isValidRatePerKm(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.ratePerKm < 0) {
            result.rejectValue("ratePerKm", "ratePerKm", "Ставка за километр не может быть отрицательной")
            return false
        }
        return true
    }


    private fun isValidDate(date: String, fieldName: String, errorMessage: String, isBirthDate: Boolean, result: BindingResult): Boolean {
        if (date.isBlank()) {
            result.rejectValue(fieldName, fieldName, "Дата не может быть пустой")
            return false
        }

        val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
        if (!dateRegex.matches(date)) {
            result.rejectValue(fieldName, fieldName, errorMessage)
            return false
        }

        val dateParts = date.split("-")
        val day = dateParts[2].toIntOrNull()
        val month = dateParts[1].toIntOrNull()
        val year = dateParts[0].toIntOrNull()

        if (day == null || month == null || year == null) {
            result.rejectValue(fieldName, fieldName, "Дата должна быть в формате yyyy-MM-dd")
            return false
        }

        if (day !in 1..31 || month !in 1..12) {
            result.rejectValue(fieldName, fieldName, "Неверный формат даты: $date")
            return false
        }

        if (isBirthDate && year >= LocalDate.now().year - 18) {
            result.rejectValue(fieldName, fieldName, "Водитель должен быть старше 18 лет")
            return false
        }

        return true
    }

    private fun isValidFuelCard(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.fuelCard.isBlank()) {
            result.rejectValue("fuelCard", "fuelCard", "Номер топливной карты не может быть пустым")
            return false
        }
        if (driverRequest.fuelCard.length !in 8..40) {
            result.rejectValue("fuelCard", "fuelCard", "Номер топливной карты должен содержать от 8 до 40 цифр")
            return false
        }
        if (fuelCardsForDriversRepository.existsByFuelCardNumber(driverRequest.fuelCard)) {
            result.rejectValue("fuelCard", "fuelCard", "Топливная карта с таким номером уже существует")
            return false
        }
        return true
    }

    private fun isValidFuelStationName(driverRequest: DriverRequest, result: BindingResult): Boolean {
        if (driverRequest.fuelStationName.isBlank()) {
            result.rejectValue("fuelStationName", "fuelStationName", "Название АЗС не может быть пустым")
            return false
        }
        if (driverRequest.fuelStationName.length >= 40) {
            result.rejectValue("fuelStationName", "fuelStationName", "Название АЗС не может быть длиннее 40 символов")
            return false
        }
        return true
    }
}

