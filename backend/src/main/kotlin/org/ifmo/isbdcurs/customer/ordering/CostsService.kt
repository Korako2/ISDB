package org.ifmo.isbdcurs.customer.ordering

import org.springframework.stereotype.Service

@Service("costsService")
class CostsService {
    fun calculatePrice(cargoParams: CargoParamsDto): Float {
        return cargoParams.length * cargoParams.width * cargoParams.height * cargoParams.weight
    }
}