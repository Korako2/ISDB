package org.ifmo.isbdcurs.models

import java.time.Instant
import java.time.LocalDate
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
    val status: OrderStatus,
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
    val dateOfBirth: LocalDate,
    val phoneNumber: String,
    val email: String,
)