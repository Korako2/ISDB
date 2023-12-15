package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AddOrderRequest
import org.ifmo.isbdcurs.persistence.VehicleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class VehicleService @Autowired constructor(
    private val vehicleRepository: VehicleRepository,
) {
    fun findSuitableVehicle(addOrderRequest: AddOrderRequest): Long {
        return vehicleRepository.findSuitableVehicle(addOrderRequest)
    }
}