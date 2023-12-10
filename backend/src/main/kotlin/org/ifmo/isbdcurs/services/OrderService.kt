package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.OrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class OrderService @Autowired constructor(
    private val orderRepo: OrderRepository,
    private val vehicleService: VehicleService
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
            orderId,
            vehicleCoordinates.latitude,
            vehicleCoordinates.longitude,
        )
        return
    }
}