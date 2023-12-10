package org.ifmo.isbdcurs.models

import java.util.*

data class AddOrderResult(
    val orderId: Long,
    val driverFullName: String,
    val driverPhoneNumber: String,
    val averageDeliveryDate: Date,
)