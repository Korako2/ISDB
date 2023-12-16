package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.internal.DriverWorker
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

    fun getAll(): List<ExtendedOrder> {
        return orderRepo.getOrderResults(2L)
    }

    fun getOrdersPage(page: Int, size: Int): List<Order> {
        return orderRepo.findAll(PageRequest.of(page, size)).toList()
    }

    fun getOrdersByCustomerId(customerId: Long, page: Int, pageSize: Int): List<Order> {
        return orderRepo.findByCustomerId(customerId, PageRequest.of(page, pageSize)).toList()
    }

    fun getById(id: Long) = orderRepo.findById(id)

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
}
