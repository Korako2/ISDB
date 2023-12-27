package org.ifmo.isbdcurs.customer.ordering

import org.ifmo.isbdcurs.customer.OrderHelperService
import org.ifmo.isbdcurs.customer.convertCustomerOrderToDto
import org.ifmo.isbdcurs.customer.data.CustomerOrderDto
import org.ifmo.isbdcurs.manager.OrderApprovalService
import org.ifmo.isbdcurs.models.CustomerOrder
import org.ifmo.isbdcurs.persistence.OrderRepository
import org.ifmo.isbdcurs.services.BackendException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class CustomerOrderService @Autowired constructor(
    private val orderRepository: OrderRepository,
    private val orderHelperService: OrderHelperService,
    private val orderApprovalService: OrderApprovalService,
) {
    fun createOrder(orderUserInput: OrderUserInput) {
        // TODO: save order to database
        println("OrderService.createOrder: $orderUserInput")
        orderRepository.addOrder()
        orderApprovalService.requestApproval()
    }

    fun getOrder(customerId: Long, orderId: Long): CustomerOrderDto {
        return orderRepository.findCustomerOrderByCustomerIdAndId(customerId, orderId).getOrNull()
            ?.let { convertCustomerOrderToDto(it) }
            ?: throw BackendException("[getOrder]: Failed to get order ${orderId} for customer ${customerId}.")
    }

    fun getActiveOrders(customerId: Long): List<CustomerOrder> {
        return orderRepository.getIncompleteOrdersByCustomerId(customerId)
    }

    fun getOrders(customerId: Long, page: Long, pageSize: Long): List<CustomerOrder> {
        return orderRepository.getExtendedResultsByCustomerId(
            limit = pageSize,
            offset = page * pageSize + 1L,
            customerId = customerId
        )
    }
}