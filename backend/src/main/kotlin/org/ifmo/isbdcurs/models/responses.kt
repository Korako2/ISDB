package org.ifmo.isbdcurs.models

import java.util.*

data class AddOrderResult(
    val orderId: Long,
    val driverFullName: String,
    val averageDeliveryDate: Date,
)

data class OrderResultDto (
    val id: Long,
    val customerName: String,
    val driverName: String,
    val departurePoint: Int,
    val deliveryPoint: Int,
    val status: String,
)