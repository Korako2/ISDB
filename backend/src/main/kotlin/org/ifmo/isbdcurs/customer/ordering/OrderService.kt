package org.ifmo.isbdcurs.customer.ordering

import org.ifmo.isbdcurs.customer.OrderHelperService
import org.ifmo.isbdcurs.customer.convertCustomerOrderToDto
import org.ifmo.isbdcurs.customer.data.CustomerOrderDto
import org.ifmo.isbdcurs.manager.OrderApprovalService
import org.ifmo.isbdcurs.persistence.LoadingUnloadingAgreementRepository
import org.ifmo.isbdcurs.persistence.OrderRepository
import org.ifmo.isbdcurs.services.BackendException
import org.ifmo.isbdcurs.util.UsersHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Service
class CustomerOrderService @Autowired constructor(
    private val orderRepository: OrderRepository,
    private val orderHelperService: OrderHelperService,
    private val orderApprovalService: OrderApprovalService,
    private val usersHelper: UsersHelper,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerOrderService::class.java)

    @Transactional
    fun createOrder(orderUserInput: OrderUserInput) {
        logger.info("[createOrder] called with $orderUserInput")
        val orderId = persistOrder(orderUserInput)
        persistAgreement(orderId, orderUserInput)
        orderApprovalService.requestApproval(orderId)
    }

    private fun persistOrder(orderUserInput: OrderUserInput): Long {
        val distance = orderHelperService.calculateDistanceBetweenAddresses(
            orderUserInput.addressesDto.delivery.id,
            orderUserInput.addressesDto.departure.id
        )
        val cargo = orderUserInput.cargo
        val persistTime = Instant.now()
        return orderRepository.addOrder(
            usersHelper.getCustomerId().toInt(),
            distance,
            cargo.weight.toDouble(),
            cargo.width.toDouble(),
            cargo.height.toDouble(),
            cargo.length.toDouble(),
            cargo.type.toString(),
            persistTime,
        )
    }

    private fun persistAgreement(orderId: Long, orderUserInput: OrderUserInput) {
        val addresses = orderUserInput.addressesDto
        val customerId = usersHelper.getCustomerId()
        val agreement = orderHelperService.createAgreement(customerId, orderId, addresses)
        loadingUnloadingAgreementRepository.save(agreement)
    }

    fun getOrder(orderId: Long): CustomerOrderDto {
        val customerId = usersHelper.getCustomerId()
        return orderRepository.findCustomerOrderByCustomerIdAndId(customerId, orderId).getOrNull()
            ?.let { convertCustomerOrderToDto(it) }
            ?: throw BackendException("[getOrder]: Failed to get order ${orderId} for customer ${customerId}.")
    }

    fun getActiveOrders(): List<CustomerOrderDto> {
        val customerId = usersHelper.getCustomerId()
        val activeOrders = orderRepository.getIncompleteOrdersByCustomerId(customerId)
            .map { convertCustomerOrderToDto(it) }
        logger.debug("[getActiveOrders] called by user $customerId and got ${activeOrders.size} active orders")
        return activeOrders
    }

    fun getCompletedOrders(page: Long, pageSize: Long): Page<CustomerOrderDto> {
        val customerId = usersHelper.getCustomerId()
        val pageable = PageRequest.of(page.toInt(), pageSize.toInt(), Sort.by("id").descending())
        val completedOrders = orderRepository.getCompletedOrdersByCustomerId(
            customerId = customerId,
            pageable = pageable
        ).map { convertCustomerOrderToDto(it) }
        logger.debug("[getCompletedOrders] called by user $customerId and got ${completedOrders.size} completed orders")
        return completedOrders
    }
}