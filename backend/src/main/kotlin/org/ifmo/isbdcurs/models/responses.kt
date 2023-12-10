package org.ifmo.isbdcurs.models

import java.util.*

data class AddOrderResult(
    val orderId: Long,
    val driverFullName: String,
    val averageDeliveryDate: Date,
)