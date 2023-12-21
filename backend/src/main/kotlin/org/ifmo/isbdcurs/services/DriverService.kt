package org.ifmo.isbdcurs.services

import jakarta.transaction.Transactional
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.persistence.DriverRepository
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DriverService @Autowired constructor(
    private val driverRepository: DriverRepository,
) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(DriverService::class.java)
    private val exceptionHelper = ExceptionHelper(logger)

    fun addDriver(addDriverRequest: AddDriverRequest) {
        driverRepository.addDriver(addDriverRequest)
    }

    fun addDriverInfo(addDriverInfoRequest: AddDriverInfoRequest) {
        driverRepository.addDriverInfo(
            addDriverInfoRequest.driverId.toInt(),
            addDriverInfoRequest.dailyRate,
            addDriverInfoRequest.ratePerKm,
            addDriverInfoRequest.issueDate,
            addDriverInfoRequest.expirationDate,
            addDriverInfoRequest.licenseNumber.toInt(),
            addDriverInfoRequest.fuelCard,
            addDriverInfoRequest.fuelStationName,
        )
    }

    fun getTotalPages(pageSize: Int): Long {
        return (driverRepository.count() + pageSize - 1) / pageSize
    }

    fun getDriversPaged(page: Int, size: Int): List<DriverResponse> {
        val minOrderId = page * size
        val maxOrderId = page * size + size
        return exceptionHelper.wrapWithBackendException("Error while getting orders") {
            driverRepository.getExtendedDriversPaged(minOrderId, maxOrderId)
        }
    }

    fun startWork(driverId: Long, orderId: Long) {
        // slowly move to order address
    }
}

