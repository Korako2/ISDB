package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.internal.DriverWorker
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalTime
import java.util.*

@Service
class OrderService @Autowired constructor(
    private val orderRepo: OrderRepository,
    private val vehicleService: VehicleService,
    private val driverWorker: DriverWorker,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    private val driverRepository: DriverRepository,
    private val personRepository: PersonRepository,
    private val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository
) {

    fun getAll(): List<Order> {
        // mock data
        val orders = mutableListOf<Order>()
        orders.add(Order(1, 1, 1.0f, 1.0, Instant.now(), 1))
        orders.add(Order(2, 2, 2.0f, 2.0, Instant.now(), 2))
        orders.add(Order(3, 3, 3.0f, 3.0, Instant.now(), 3))
        return orders
    }

    fun getById(id: Long) = orderRepo.findById(id)

    fun create(order: Order) = orderRepo.save(order)

    fun update(id: Long, order: Order) = orderRepo.save(order.copy(id = id))

    fun delete(id: Long) = orderRepo.deleteById(id)

    fun addOrder(addOrderRequest: AddOrderRequest): AddOrderResult {
        val vehicleId = vehicleService.findSuitableVehicle(addOrderRequest)
        val vehicleCoordinates = vehicleService.getVehicleCoordinates(vehicleId)
        val orderCoordinates = Coordinates(addOrderRequest.latitude, addOrderRequest.longitude)

        val driverId = vehicleOwnershipRepository.findByVehicleId(vehicleId).driverId
        val personId = driverRepository.findPersonIdById(driverId)
        val driverFullName =
            personRepository.findById(personId).get().firstName + " " + personRepository.findById(personId)
                .get().lastName

        // create agreement
        val loadingUnloadingAgreement = LoadingUnloadingAgreement(
            orderId = 0,
            driverId = driverId,
            unloadingTime = LocalTime.ofSecondOfDay(addOrderRequest.unloadingTimeSec),
            loadingTime = LocalTime.ofSecondOfDay(addOrderRequest.loadingTimeSec),
            departurePoint = addOrderRequest.departurePointId,
            deliveryPoint = addOrderRequest.deliveryPointId,
            senderId = addOrderRequest.senderId,
            receiverId = addOrderRequest.receiverId,
        )

        loadingUnloadingAgreementRepository.save(loadingUnloadingAgreement)

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
        )
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