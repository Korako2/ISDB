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