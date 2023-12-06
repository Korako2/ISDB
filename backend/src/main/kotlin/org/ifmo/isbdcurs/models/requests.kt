package org.ifmo.isbdcurs.models

data class AddCustomerRequest(
    val firstName : String,
    val lastName : String,
    val middleName : String,
    val gender : String,
    val dateOfBirth : String,
)

data class AddDriverRequest(
    val firstName : String,
    val lastName : String,
    val middleName: String,
    val gender : String,
    val dateOfBirth : String,
    val passport : String,
    val bankCardNumber : String,
)

data class AddDriverInfoRequest(
    val driverId : Long,
    val dailyRate : Int,
    val ratePerKm : Int,
    val issueDate : String,
    val expirationDate : String,
    val licenseNumber : Long,
    val fuelCards : List<String>,
    val fuelStationNames : List<String>,
)

data class AddOrderRequest(
    val customerId : Long,
    val distance : Int,
    val vehicleId : Long,
    val weight : Int,
    val width : Int,
    val height : Int,
    val length : Int,
    val cargoType : String,
)