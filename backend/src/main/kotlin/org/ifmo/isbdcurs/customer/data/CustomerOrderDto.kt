package org.ifmo.isbdcurs.customer.data

import java.time.Instant

data class CustomerOrderDto(
    val statusChangedTime: Instant,
    val driverName: String,
    val departureAddress: String,
    val deliveryAddress: String,
    val status: String,
)