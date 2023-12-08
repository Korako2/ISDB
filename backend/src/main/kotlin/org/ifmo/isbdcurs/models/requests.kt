package org.ifmo.isbdcurs.models

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