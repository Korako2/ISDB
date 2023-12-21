package org.ifmo.isbdcurs.internal

import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


data class DriverState(
    val driverId: Long,
    val orderId: Long,
    val vehicle: Vehicle,
    var currentPosition: Coordinates,
    var currentDriverStatus: DriverStatus,
    var mileage: Float,
)

@Service
class DriverWorker(
    val vehicleMovementHistoryRepository: VehicleMovementHistoryRepository,
    val driverStatusHistoryRepository: DriverStatusHistoryRepository,
    val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    val storagePointRepository: StoragePointRepository,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    private val vehicleRepository: VehicleRepository
) {
    private val driverToState = ConcurrentHashMap<Long, DriverState>()
    private val kmPerHour: Int = 60
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverWorker::class.java)
    private val disableDelay = true

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

    private fun sleepSecs(secs: Long) {
        if (disableDelay) return
        Thread.sleep(secs * 1000)
    }

    private fun move(driverState: DriverState, destination: Coordinates) {
        val currentPosition = driverState.currentPosition
        val timeToNextCoordinate = calculateTimeToNextCoordinate(currentPosition, destination)
        val sleepTime = timeToNextCoordinate.toLong()
        logger.debug("Driver {} is moving to {} with speed {} km/h, will arrive in {} seconds", driverState.driverId, destination, kmPerHour, timeToNextCoordinate)
        sleepSecs(sleepTime)
        driverState.currentPosition = destination
        driverState.mileage += currentPosition.calcDistanceKm(destination).toFloat()

        val newMovementHistory = VehicleMovementHistory(
            date = Instant.now(),
            vehicleId = driverState.vehicle.id!!,
            latitude = currentPosition.latitude.toFloat(),
            longitude = currentPosition.longitude.toFloat(),
            mileage = driverState.mileage
        )
        vehicleMovementHistoryRepository.save(newMovementHistory)
    }

    private fun moveToDeparturePoint(driverState: DriverState) {
        val destination = getDeparturePoint(driverState.orderId, driverState.driverId)
        move(driverState, destination)
        logger.debug("Driver is at departure point {}", destination)
    }

    private fun setDriverStatus(driverState: DriverState, newStatus: DriverStatus) {
        driverState.currentDriverStatus = newStatus
        val driverId = driverState.driverId
        logger.debug("Driver {} changed status from {} to {}. At {}", driverState.driverId, driverState.currentDriverStatus, newStatus, Instant.now())
        val newDriverStatus = DriverStatusHistory(
            driverId = driverId,
            date = Instant.now(),
            status = driverState.currentDriverStatus
        )
        driverStatusHistoryRepository.save(newDriverStatus)
    }

    private fun setDriverStatusAfterDelay(driverState: DriverState, newStatus: DriverStatus, delaySec: Long) {
        sleepSecs(delaySec)
        setDriverStatus(driverState, newStatus)
    }

    private fun moveToDeliveryPoint(driverState: DriverState, destination: Coordinates) {
        logger.debug("Driver {} is moving to {}", driverState.driverId, destination)
        move(driverState, destination)
        logger.debug("Driver state after move: {}", driverState)
    }

    @Transactional
    fun startWork(driverId: Long, orderId: Long) {
        logger.debug("Driver {} started work on order {}", driverId, orderId)

        val vehicleId = vehicleOwnershipRepository.findByDriverId(driverId).last().vehicleId
        val vehicle = vehicleRepository.findById(vehicleId).get()
        val departureCord = getDeparturePoint(orderId, driverId)
        val mileage = vehicleMovementHistoryRepository.findByVehicleIdOrderByDateDesc(vehicle.id!!).firstOrNull()?.mileage ?: 0.0f
        val driverStatus = driverStatusHistoryRepository.findByDriverIdOrderByDateDesc(driverId).firstOrNull()?.status ?: DriverStatus.READY_FOR_NEW_ORDER
        val defaultDriverState = DriverState(driverId=driverId, orderId=orderId, vehicle=vehicle, currentDriverStatus = driverStatus, currentPosition = departureCord, mileage = mileage)
        val ds: DriverState = this.driverToState.getOrDefault(driverId, defaultDriverState)

        setDriverStatus(ds, DriverStatus.OFF_DUTY)
        setDriverStatus(ds, DriverStatus.ACCEPTED_ORDER)
        moveToDeparturePoint(ds)
        setDriverStatus(ds, DriverStatus.ARRIVED_AT_LOADING_LOCATION)
        setDriverStatusAfterDelay(ds, DriverStatus.LOADING, 4)
        setDriverStatusAfterDelay(ds, DriverStatus.EN_ROUTE, 3)

        moveToDeliveryPoint(ds, departureCord)
        setDriverStatus(ds, DriverStatus.ARRIVED_AT_UNLOADING_LOCATION)
        setDriverStatusAfterDelay(ds, DriverStatus.UNLOADING, 4)
        setDriverStatusAfterDelay(ds, DriverStatus.COMPLETED_ORDER, 5)
        setDriverStatusAfterDelay(ds, DriverStatus.READY_FOR_NEW_ORDER, 10)
        logger.debug("Driver {} finished work on order {}", driverId, orderId)
    }
}