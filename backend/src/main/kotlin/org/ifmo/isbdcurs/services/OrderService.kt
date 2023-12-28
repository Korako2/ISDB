package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.customer.OrderHelperService
import org.ifmo.isbdcurs.internal.DriverWorker
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.ifmo.isbdcurs.util.pageToIdRangeReversed
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import java.time.Instant
import kotlin.concurrent.thread

@Component
class OrderTransactionHelper(
    private val orderRepository: OrderRepository,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    private val driverStatusHistoryRepository: DriverStatusHistoryRepository
) {
    @Transactional
    fun updateOrderAndAgreement(orderId: Long, vehicleId: Long, driverId: Long) {
        orderRepository.updateVehicleIdById(id = orderId, vehicleId = vehicleId)
        loadingUnloadingAgreementRepository.updateDriverIdByOrderId(orderId = orderId, driverId = driverId)
    }

    @Transactional
    fun updateDriverStatus(driverId: Long) {
        val driverStatusHistory = DriverStatusHistory(
            driverId = driverId,
            date = Instant.now(),
            status = DriverStatus.ACCEPTED_ORDER
        )
        driverStatusHistoryRepository.save(driverStatusHistory)
    }
}

@Service
class OrderService @Autowired constructor(
    private val orderRepo: OrderRepository,
    private val vehicleService: VehicleService,
    private val driverWorker: DriverWorker,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    private val storagePointRepository: StoragePointRepository,
    private val addressRepository: AddressRepository,
    private val orderStatusesRepository: OrderStatusesRepository,
    private val orderHelperService: OrderHelperService,
    private val orderTransactionHelper: OrderTransactionHelper,
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(OrderService::class.java)

    private val availableCountries = arrayOf("Россия")

    private val exceptionHelper = ExceptionHelper(logger)

    fun getOrdersPaged(page: Int, pageSize: Int): List<OrderResponse> {
        val totalPages = getTotalPages(pageSize).toInt()
        val (startOrderId, endOrderId) = pageToIdRangeReversed(totalPages, page, pageSize)
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            orderRepo.getExtendedResults(startOrderId, endOrderId).map { it.toOrderResponse() }
        }
    }

    fun getTotalPages(pageSize: Int): Long {
        return (orderRepo.count() + pageSize - 1) / pageSize
    }

    fun getTotalPagesForManager(pageSize: Int): Long {
        return (orderStatusesRepository.countByOrderStatus() + pageSize - 1) / pageSize
    }

    fun countCustomerPages(customerId: Long, pageSize: Int): Int {
        return (orderRepo.countByCustomerId(customerId) + pageSize - 1) / pageSize
    }

    // gets order from database in reverse order. Raises exception if order not found or jpa error
    fun getOrdersByCustomerId(customerId: Long, page: Int, pageSize: Int): List<CustomerOrderResponse> {
        val offset = page * pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting orders by customer id") {
            val orders = orderRepo.getExtendedResultsByCustomerId(customerId, pageSize.toLong(), offset.toLong()).map {
                it.toCustomerOrderResponse()
            }
            logger.info(
                "Getting orders for customer with id = $customerId. Page = $page, pageSize = $pageSize. " +
                        "Orders size = ${orders.size}. Offset = $offset"
            )
            orders
        }
    }

    fun getOrdersForManager(page: Int, pageSize: Int): List<ManagerOrderResponse> {
        val offset = page * pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting orders by for manager's page") {
            val orders = orderRepo.getResultsForManager(pageSize, offset).map {
                it.toManagerOrderResponse()
            }
            logger.info(
                "Page = $page, pageSize = $pageSize. " +
                        "Orders size = ${orders.size}. Offset = $offset"
            )
            orders
        }
    }

    fun getFullOrdersInfo(page: Int, pageSize: Int): List<FullOrdersInfoResponse> {
        val offset = page * pageSize
        return exceptionHelper.wrapWithBackendException("Error while getting full information about orders") {
            val orders = orderRepo.getFullOrdersInfoForManager(pageSize, offset).map {
                it.toFullOrderInfoResponse()
            }
            logger.info(
                "Page = $page, pageSize = $pageSize. " +
                        "Orders size = ${orders.size}. Offset = $offset"
            )
            orders
        }
    }

    fun getFullOrderInfoById(orderId: Long): FullOrdersInfoResponse {
        return exceptionHelper.wrapWithBackendException("Error while getting full information about order") {
            var order = orderRepo.getFullOrderInfoById(orderId).toFullOrderInfoResponse()
            logger.info(
                "Order id = $orderId. " +
                        "Order = $order"
            )
            order
        }
    }

    fun rejectOrder(orderId: Long) {
        exceptionHelper.wrapWithBackendException("Error while rejecting order") {
            orderRepo.deleteOrderById(orderId)
        }
    }

    fun findSuitableDriver(orderId: Long): Long {
        return exceptionHelper.wrapWithBackendException("Error while finding suitable driver") {
            val cargo = orderHelperService.getCargoParamsByOrderId(orderId)
            val departureCoordinates = orderHelperService.getDepartureCoordinatesByOrderId(orderId)
            val orderDataForVehicle = OrderDataForVehicle(
                weight = cargo.weight.toDouble(),
                width = cargo.width.toDouble(),
                height = cargo.height.toDouble(),
                length = cargo.length.toDouble(),
                cargoType = cargo.type,
                latitude = departureCoordinates.latitude,
                longitude = departureCoordinates.longitude,
            )
            val vehicleId = vehicleService.findSuitableVehicle(orderDataForVehicle)
            if (vehicleId == -1L) {
                throw BackendException("Водитель для заказа ${orderId} не найден. Попробуйте позже или обратитесь к старшему менеджеру")
            }
            val driverId = vehicleOwnershipRepository.findByVehicleId(vehicleId).driverId
            driverId
        }
    }

    @Transactional
    fun updateOrderWhenVehicleFound(orderId: Long, vehicleId: Long, driverId: Long) {
        orderTransactionHelper.updateOrderAndAgreement(orderId, vehicleId, driverId)
        orderTransactionHelper.updateDriverStatus(driverId)
    }

    @Transactional
    fun startDriverWorker(driverId: Long, orderId: Long) {
        thread {
            driverWorker.startWork(driverId = driverId, orderId = orderId)
        }
    }

    private fun isValidCountry(country: String): Boolean = country in availableCountries

    private fun rejectInvalidValue(result: BindingResult, field: String, errorCode: String, errorMessage: String) {
        result.rejectValue(field, errorCode, errorMessage)
    }

    private fun ExtendedOrder.toOrderResponse(): OrderResponse {
        return OrderResponse(
            id = this.id,
            customerName = this.customerName,
            driverName = this.driverName ?: "не назначен",
            departurePoint = this.departurePoint,
            deliveryPoint = this.deliveryPoint,
            status = this.status.translate(),
        )
    }

    private fun CustomerOrder.toCustomerOrderResponse(): CustomerOrderResponse {
        return CustomerOrderResponse(
            statusChangedTime = this.statusChangedTime,
            driverName = this.driverName ?: "не назначен",
            departureAddress = this.departureAddress,
            deliveryAddress = this.deliveryAddress,
            status = this.status.translate(),
        )
    }

    private fun ManagerOrder.toManagerOrderResponse(): ManagerOrderResponse {
        return ManagerOrderResponse(
            id = this.id,
            statusChangedTime = this.statusChangedTime,
            phoneNumber = this.value,
            departureAddress = this.departureAddress,
            deliveryAddress = this.deliveryAddress,
            status = this.status.translate(),
        )
    }

    private fun FullOrdersInfo.toFullOrderInfoResponse(): FullOrdersInfoResponse {
        return FullOrdersInfoResponse(
            id = this.id,
            statusChangedTime = this.statusChangedTime,
            phoneNumber = this.value,
            customerFirstName = this.customerFirstName,
            customerLastName = this.customerLastName,
            cargo = this.cargo,
            cargoType = this.cargo.cargoType.translatedName,
            departureAddress = this.departureAddress,
            deliveryAddress = this.deliveryAddress,
            loadingTime = this.loadingTime,
            unloadingTime = this.unloadingTime,
            status = this.status.translate()
        )
    }

}
