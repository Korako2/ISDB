package org.ifmo.isbdcurs.models

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class AddOrderResult(
    val orderId: Long,
    val driverFullName: String,
    val averageDeliveryDate: Date,
)

data class OrderResponse (
    val id: Long,
    val customerName: String,
    val driverName: String,
    val departurePoint: Long,
    val deliveryPoint: Long,
    val status: String,
)

data class CustomerOrderResponse (
    val statusChangedTime: Instant,
    val driverName: String,
    val departureAddress: Address,
    val deliveryAddress: Address,
    val status: String,
)

data class DriverResponse (
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val licenseNumber: Int,
    val issueDate: Instant,
    val expirationDate: Instant,
    val carNumber: String
)

data class CustomerResponse (
    val id: Long,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Instant,
    val phoneNumber: String,
    val email: String,
)

data class ManagerOrderResponse (
    val id: Long,
    val statusChangedTime: Instant,
    val phoneNumber: String,
    val departureAddress: Address,
    val deliveryAddress: Address,
    val status: String,
)

data class FullOrderInfoResponse (
    val id: Long,
    val statusChangedTime: Instant,
    val phoneNumber: String,
    val customerFirstName: String,
    val customerLastName: String,
    val cargo: Cargo,
    val loadingTime: LocalTime,
    val unloadingTime: LocalTime,
    val departureAddress: Address,
    val deliveryAddress: Address,
    val status: String
)
