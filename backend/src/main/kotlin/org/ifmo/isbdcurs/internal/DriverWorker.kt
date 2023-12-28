package org.ifmo.isbdcurs.internal

import org.ifmo.isbdcurs.manager.OrderApprovalService
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.*
import org.springframework.stereotype.Component
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


const val DELAY_BETWEEN_MOVE_SEC = 5L

@Component
class TransactionalMove(
    private val vehicleMovementHistoryRepository: VehicleMovementHistoryRepository,
    private val driverStatusHistoryRepository: DriverStatusHistoryRepository
) {
    @Transactional
    fun move(movementHistory: VehicleMovementHistory) {
        vehicleMovementHistoryRepository.save(movementHistory)
    }

    @Transactional
    fun updateStatusHistory(driverStatusHistory: DriverStatusHistory) {
        driverStatusHistoryRepository.save(driverStatusHistory)
    }
}

@Service
class DriverWorker(
    val vehicleMovementHistoryRepository: VehicleMovementHistoryRepository,
    val driverStatusHistoryRepository: DriverStatusHistoryRepository,
    val loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository,
    val storagePointRepository: StoragePointRepository,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    private val vehicleRepository: VehicleRepository,
    private val transactionalMove: TransactionalMove,
    private val approvalService: OrderApprovalService,
) {
    private val driverToState = ConcurrentHashMap<Long, DriverState>()
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverWorker::class.java)
    private val disableDelay = false

    private fun getAddressById(addrId: Long): Coordinates {
        val storagePoint = storagePointRepository.findById(addrId).get()
        return Coordinates(storagePoint.latitude.toDouble(), storagePoint.longitude.toDouble())
    }

    private fun getDeparturePoint(orderId: Long): Coordinates {
        val addressId = loadingUnloadingAgreementRepository.findByOrderId(orderId = orderId)!!.departurePoint
        return getAddressById(addressId)
    }

    private fun sleepSecs(seconds: Long = DELAY_BETWEEN_MOVE_SEC) {
        if (disableDelay) return
        Thread.sleep(seconds * 1000)
    }

    fun move(driverState: DriverState, destination: Coordinates) {
        val currentPosition = driverState.currentPosition
        logger.debug("Driver {} is moving to {}", driverState.driverId, destination)
        sleepSecs()
        driverState.currentPosition = destination
        driverState.mileage += currentPosition.calcDistanceKm(destination).toFloat()

        val newMovementHistory = VehicleMovementHistory(
            date = Instant.now(),
            vehicleId = driverState.vehicle.id!!,
            latitude = currentPosition.latitude.toFloat(),
            longitude = currentPosition.longitude.toFloat(),
            mileage = driverState.mileage
        )
        transactionalMove.move(newMovementHistory)
    }

    private fun moveToDeparturePoint(driverState: DriverState) {
        val destination = getDeparturePoint(driverState.orderId)
        move(driverState, destination)
        logger.debug("Driver is at departure point {}", destination)
    }

    private fun setDriverStatus(driverState: DriverState, newStatus: DriverStatus) {
        driverState.currentDriverStatus = newStatus
        val driverId = driverState.driverId
        logger.debug(
            "Driver {} changed status from {} to {}. At {}",
            driverState.driverId,
            driverState.currentDriverStatus,
            newStatus,
            Instant.now()
        )
        val newDriverStatus = DriverStatusHistory(
            driverId = driverId,
            date = Instant.now(),
            status = driverState.currentDriverStatus
        )
        transactionalMove.updateStatusHistory(newDriverStatus)
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
        val departureCord = getDeparturePoint(orderId)
        val mileage =
            vehicleMovementHistoryRepository.findByVehicleIdOrderByDateDesc(vehicle.id!!).firstOrNull()?.mileage ?: 0.0f
        val driverStatus = driverStatusHistoryRepository.findByDriverIdOrderByDateDesc(driverId).firstOrNull()?.status
            ?: DriverStatus.READY_FOR_NEW_ORDER
        val defaultDriverState = DriverState(
            driverId = driverId,
            orderId = orderId,
            vehicle = vehicle,
            currentDriverStatus = driverStatus,
            currentPosition = departureCord,
            mileage = mileage
        )
        val ds: DriverState = this.driverToState.getOrDefault(driverId, defaultDriverState)

        moveToDeparturePoint(ds)
        setDriverStatus(ds, DriverStatus.ARRIVED_AT_LOADING_LOCATION)
        setDriverStatusAfterDelay(ds, DriverStatus.LOADING, 2)
        setDriverStatusAfterDelay(ds, DriverStatus.EN_ROUTE, 2)

        moveToDeliveryPoint(ds, departureCord)
        setDriverStatus(ds, DriverStatus.ARRIVED_AT_UNLOADING_LOCATION)
        setDriverStatusAfterDelay(ds, DriverStatus.UNLOADING, 2)
        setDriverStatusAfterDelay(ds, DriverStatus.COMPLETED_ORDER, 2)
        setDriverStatusAfterDelay(ds, DriverStatus.READY_FOR_NEW_ORDER, 2)
        logger.debug("Driver {} finished work on order {}", driverId, orderId)
        approvalService.orderComplete(orderId)
    }
}