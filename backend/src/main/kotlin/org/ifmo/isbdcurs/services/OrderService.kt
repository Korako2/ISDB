package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.internal.DriverWorker
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import java.time.Instant
import java.time.LocalTime
import java.util.*
import kotlin.jvm.optionals.getOrElse

@Service
class OrderService @Autowired constructor(
    private val orderRepo: OrderRepository,
    private val vehicleService: VehicleService,
    private val driverWorker: DriverWorker,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    private val personRepository: PersonRepository,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    private val vehicleMovementHistoryRepository: VehicleMovementHistoryRepository,
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverWorker::class.java)
    private val availableCountries = arrayOf("Россия")

    private fun ExtendedOrder.toOrderResponse(): OrderResponse {
        return OrderResponse(
            id = this.id,
            customerName = this.customerName,
            driverName = this.driverName,
            departurePoint = this.departurePoint,
            deliveryPoint = this.deliveryPoint,
            status = this.status,
        )
    }

    fun getOrdersPaged(page: Int, size: Int): List<OrderResponse> {
        val minOrderId = page * size
        val maxOrderId = page * size + size
        return orderRepo.getExtendedResults(minOrderId, maxOrderId).map { it.toOrderResponse() }
    }

    fun getOrdersByCustomerId(customerId: Long, page: Int, pageSize: Int): List<OrderResponse> {
        val minOrderId = page * pageSize
        val maxOrderId = page * pageSize + pageSize
        return orderRepo.getExtendedResultsByCustomerId(customerId, minOrderId, maxOrderId).map { it.toOrderResponse() }
    }

    fun create(order: Order) = orderRepo.save(order)

    fun update(id: Long, order: Order) = orderRepo.save(order.copy(id = id))

    fun delete(id: Long) = orderRepo.deleteById(id)

    @Transactional
    fun addOrder(addOrderRequest: AddOrderRequest): AddOrderResult {
        val vehicleId = vehicleService.findSuitableVehicle(addOrderRequest)
        if (vehicleId == -1L) {
            throw Exception("No suitable vehicle found")
        }

        val vehicleCoordinates =
            vehicleMovementHistoryRepository.findByVehicleIdOrderByDateDesc(vehicleId).firstOrNull()?.let {
                Coordinates(it.latitude.toDouble(), it.longitude.toDouble())
            } ?: Coordinates(0.0, 0.0)
        val orderCoordinates = Coordinates(addOrderRequest.latitude, addOrderRequest.longitude)

        logger.debug("Found nearest vehicle with id = $vehicleId")
        val driverId = vehicleOwnershipRepository.findByVehicleId(vehicleId).driverId
        val person = personRepository.findById(driverId).getOrElse { throw Exception("Driver not found") }
        val driverFullName = person.firstName + " " + person.lastName

        val driveToAddressDistance = vehicleCoordinates.calcDistanceKm(orderCoordinates)
        // TODO: get current customer id from session
        val customerId = 1L

        val orderId = orderRepo.addOrder(
            customerId.toInt(),
            addOrderRequest.distance,
            vehicleId.toInt(),
            addOrderRequest.weight,
            addOrderRequest.width,
            addOrderRequest.height,
            addOrderRequest.length,
            addOrderRequest.cargoType,
            Date.from(Instant.now()),
        )

        logger.debug("New Order id = $orderId")

        val unloadingSeconds = addOrderRequest.unloadingTime * 60 * 60
        val loadingSeconds = addOrderRequest.loadingTime * 60 * 60
        // create agreement
        val loadingUnloadingAgreement = LoadingUnloadingAgreement(
            orderId = orderId,
            driverId = driverId,
            unloadingTime = LocalTime.ofSecondOfDay(unloadingSeconds),
            loadingTime = LocalTime.ofSecondOfDay(loadingSeconds),
            departurePoint = addOrderRequest.departurePointId,
            deliveryPoint = addOrderRequest.deliveryPointId,
            senderId = addOrderRequest.senderId,
            receiverId = addOrderRequest.receiverId,
        )

        loadingUnloadingAgreementRepository.save(loadingUnloadingAgreement)

        println("===================== orderId = $orderId")
        val addOrderResult = AddOrderResult(
            orderId = orderId,
            averageDeliveryDate = Date.from(Instant.now().plusSeconds((driveToAddressDistance / 60).toLong())),
            driverFullName = driverFullName,
        )

        run {
            driverWorker.startWork(driverId = driverId, orderId = orderId)
        }
        return addOrderResult
    }

    fun isValidData(orderDataRequest: OrderDataRequest, result: BindingResult): Boolean {
        return isValidAddresses(orderDataRequest, result) && isValidCargoType(orderDataRequest.cargoType)
    }

    private fun isValidCountry(country: String): Boolean = country in availableCountries

    private fun rejectInvalidValue(result: BindingResult, field: String, errorCode: String, errorMessage: String) {
        result.rejectValue(field, errorCode, errorMessage)
    }

    private fun isValidAddresses(orderDataRequest: OrderDataRequest, result: BindingResult): Boolean {
        if (!isValidCountry(orderDataRequest.departureCountry)) {
            logger.warn("[OrderService] isValidAddresses: departureCountry = ${orderDataRequest.departureCountry}")
            rejectInvalidValue(result, "departureCountry", "error.departureCountry", "Страна не поддерживается")
            return false
        }

        if (!isValidCountry(orderDataRequest.destinationCountry)) {
            logger.warn("[OrderService] isValidAddresses: destinationCountry = ${orderDataRequest.destinationCountry}")
            rejectInvalidValue(result, "destinationCountry", "error.destinationCountry", "Страна не поддерживается")
            return false
        }

        if (orderDataRequest.departureCountry != orderDataRequest.destinationCountry) {
            logger.warn("[OrderService] isValidAddresses: departureCountry = ${orderDataRequest.departureCountry}, destinationCountry = ${orderDataRequest.destinationCountry}")
            rejectInvalidValue(result, "destinationCountry", "error.destinationCountry", "Страны отправления и назначения должны совпадать")
            return false
        }

        return true
    }
    private fun isValidCargoType(cargoType: String): Boolean {
        return cargoType in arrayOf("BULK", "TIPPER", "PALLETIZED")
    }


}
