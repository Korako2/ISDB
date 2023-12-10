package org.ifmo.isbdcurs.internal

import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.springframework.stereotype.Service
import java.time.Instant


data class DriverState(
    val driverId: Long,
    val orderId: Long,
    val vehicle: Vehicle,
    var currentPosition: Coordinates,
    var currentDriverStatus: DriverStatus,
)

@Service
class DriverWorker constructor(
    val driverRepository: DriverRepository,
    val vehicleMovementHistoryRepository: VehicleMovementHistoryRepository,
    val vehicleRepository: VehicleRepository,
    val driverStatusHistoryRepository: DriverStatusHistoryRepository,
    val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    val storagePointRepository: StoragePointRepository
) {
    private val driverToState = mutableMapOf<Long, DriverState>()
    private val kmPerHour: Int = 60
    private var currentDriverStatus: DriverStatus = DriverStatus.OFF_DUTY
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverWorker::class.java)

    private fun calculateTimeToNextCoordinate(currentPosition: Coordinates, coordinates: Coordinates): Double {
        val distance = currentPosition.calcDistanceKm(coordinates)
        return distance / kmPerHour
    }

    private fun getAddressById(addrId: Long): Coordinates {
        val storagePoint = storagePointRepository.findById(addrId).get()
        return Coordinates(storagePoint.latitude.toDouble(), storagePoint.longitude.toDouble())
    }

    private fun getDeparturePoint(orderId: Long, driverId: Long): Coordinates {
        val addressId = loadingUnloadingAgreementRepository.findByOrderIdAndDriverId(orderId = orderId, driverId = driverId)!!.departurePoint
        return getAddressById(addressId)
    }

    private fun getDeliveryPoint(orderId: Long, driverId: Long): Coordinates {
        val addressId = loadingUnloadingAgreementRepository.findByOrderIdAndDriverId(orderId = orderId, driverId = driverId)!!.deliveryPoint
        return getAddressById(addressId)
    }

    private fun move(driverState: DriverState, destination: Coordinates): Long {
        val currentPosition = driverState.currentPosition
        val timeToNextCoordinate = calculateTimeToNextCoordinate(currentPosition, destination)
        Thread.sleep((timeToNextCoordinate * 1000).toLong())
        driverState.currentPosition = destination
        return currentPosition.calcDistanceKm(destination).toLong()
    }

    private fun moveToDeparturePoint(driverState: DriverState) {
        move(driverState, getDeparturePoint(driverState.orderId, driverState.driverId))
    }

    private fun stepAndUpdateStatus(driverState: DriverState, destination: Coordinates) {
        val distance = move(driverState, destination)

        val driverId = driverState.driverId
        val currentPosition = driverState.currentPosition
        val vehicle = driverRepository.getVehicleByDriverId(driverId)
        if (currentDriverStatus == DriverStatus.READY_FOR_NEW_ORDER) {
            logger.debug("Driver is finished and ready for new order")
            return
        }
        val nextStatus = DriverStatus.values()[currentDriverStatus.ordinal + 1]
        logger.debug("Driver status changed from {} to {}", currentDriverStatus, nextStatus)
        currentDriverStatus = nextStatus

        val newMovementHistory = VehicleMovementHistory(
            date = Instant.now(),
            vehicleId = vehicle.id!!,
            latitude = currentPosition.latitude.toFloat(),
            longitude = currentPosition.longitude.toFloat(),
            mileage = 0.0f  // TODO: calculate mileage
        )
        val newDriverStatus = DriverStatusHistory(
            driverId = driverId,
            date = Instant.now(),
            status = currentDriverStatus
        )
        vehicleMovementHistoryRepository.save(newMovementHistory)
        driverStatusHistoryRepository.save(newDriverStatus)
    }

    fun startWork(driverId: Long, orderId: Long) {
        val vehicle = driverRepository.getVehicleByDriverId(driverId)
        val departureCord = getDeparturePoint(orderId, driverId)
        val
        val defaultDriverState = DriverState(driverId=driverId, orderId=orderId, vehicle=vehicle, currentDriverStatus = DriverStatus.OFF_DUTY, currentPosition = departureCord)
        val ds: DriverState = this.driverToState.getOrDefault(driverId, defaultDriverState)

        moveToDeparturePoint(ds)

        val driver = driverRepository.getDriverById(driverId)
        // slowly move to order address
    }
}