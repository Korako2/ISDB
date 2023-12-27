package org.ifmo.isbdcurs.customer.ordering

import org.springframework.stereotype.Service

@Service
class CustomerOrderService() {
    fun createOrder(orderDetails: OrderDetails) {
        println("OrderService.createOrder: $orderDetails")
    }

    fun getOrder(orderId: Long): OrderDetails {
        println("OrderService.getOrder: $orderId")
        return OrderDetails(
            1,
            AddressesDto(
                AddressDto(1, "Moscow"),
                AddressDto(2, "New York")
            ),
            CargoParamsDto("OPEN", 1.0f, 1.0f, 1.0f, 1.0f),
            150.0f
        )
    }

    fun getOrders(): List<OrderDetails> {
        println("OrderService.getOrders")
        return listOf(
            OrderDetails(
                1,
                AddressesDto(
                    AddressDto(1, "Moscow"),
                    AddressDto(2, "New York")
                ),
                CargoParamsDto("OPEN", 1.0f, 1.0f, 1.0f, 1.0f),
                150.0f
            ),
            OrderDetails(
                1,
                AddressesDto(
                    AddressDto(3, "Moscow"),
                    AddressDto(4, "New York")
                ),
                CargoParamsDto("OPEN", 1.0f, 1.0f, 1.0f, 1.0f),
                100.0f
            ),
        )
    }
}