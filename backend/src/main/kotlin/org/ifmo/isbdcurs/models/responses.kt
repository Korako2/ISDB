package org.ifmo.isbdcurs.models

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
    val licenseNumber: String,
    val issueDate: Date,
    val expirationDate: Date,
    val carNumber: String
)