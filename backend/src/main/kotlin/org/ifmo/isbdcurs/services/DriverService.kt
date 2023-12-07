package org.ifmo.isbdcurs.services

import jakarta.transaction.Transactional
import org.ifmo.isbdcurs.models.AddDriverInfoRequest
import org.ifmo.isbdcurs.persistence.DriverRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DriverService @Autowired constructor(
    private val driverRepository: DriverRepository,
) {
    @Transactional
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
}

