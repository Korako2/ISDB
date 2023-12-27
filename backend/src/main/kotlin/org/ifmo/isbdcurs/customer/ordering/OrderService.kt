package org.ifmo.isbdcurs.customer.ordering

import org.ifmo.isbdcurs.persistence.OrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CustomerOrderService @Autowired constructor(
    private val orderRepository: OrderRepository
) {
    fun createOrder(orderDetails: OrderDetails) {
        // TODO: save order to database
        println("OrderService.createOrder: $orderDetails")
    }

    fun getOrder(orderId: Long): OrderDetails {
        val order = orderRepository.findById(orderId).orElseThrow()
        order.
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