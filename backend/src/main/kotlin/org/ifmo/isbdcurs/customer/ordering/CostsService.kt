package org.ifmo.isbdcurs.customer.ordering

import org.ifmo.isbdcurs.util.calculateCargoCost
import org.springframework.stereotype.Service

@Service("costsService")
class CostsService {
    fun calculatePrice(cargoParams: CargoParamsDto): Float {
        return calculateCargoCost(
            cargoParams.type,
            cargoParams.weight,
            cargoParams.height,
            cargoParams.width,
            cargoParams.length,
        )
    }
}