package org.ifmo.isbdcurs.models

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
)

fun Coordinates.calcDistanceKm(other: Coordinates): Double {
    val lat1 = this.latitude
    val lon1 = this.longitude
    val lat2 = other.latitude
    val lon2 = other.longitude
    val theta = lon1 - lon2
    var dist = sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            cos(Math.toRadians(theta))
    dist = acos(dist)
    dist = Math.toDegrees(dist)
    dist *= 60 * 1.1515
    dist *= 1.609344
    return dist
}