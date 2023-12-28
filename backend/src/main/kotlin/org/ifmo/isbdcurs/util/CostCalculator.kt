package org.ifmo.isbdcurs.util


//CargoType.BULK -> "Сыпучие"
//CargoType.TIPPER -> "Самосвал"
//CargoType.PALLETIZED -> "Паллеты"

fun calculateCargoCost(
    cargoType: String,
    weight: Float,
    height: Float,
    width: Float,
    length: Float,
): Float {
    val volume = height * width * length + weight
    return when (cargoType) {
        "Сыпучие" -> volume * 1218
        "Самосвал" -> volume * 800
        "Паллеты" -> volume * 348
        else -> volume * 200
    }
}

fun calculateDeliveryCost(
    cargoCost: Float,
    distance: Float,
) = cargoCost + (distance * 20)