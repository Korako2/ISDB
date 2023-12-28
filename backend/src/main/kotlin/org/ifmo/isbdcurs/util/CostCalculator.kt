package org.ifmo.isbdcurs.util

import org.ifmo.isbdcurs.models.CargoType

fun calculateCargoCost(
    cargoType: CargoType,
    weight: Float,
    height: Float,
    width: Float,
    length: Float,
): Float {
    val volume = height * width * length + weight
    return when (cargoType) {
        CargoType.BULK -> volume * 100
        CargoType.TIPPER -> volume * 150
        CargoType.PALLETIZED -> volume * 200
    }
}

fun calculateDeliveryCost(
    cargoCost: Float,
    distance: Float,
) = cargoCost + (distance * 20)