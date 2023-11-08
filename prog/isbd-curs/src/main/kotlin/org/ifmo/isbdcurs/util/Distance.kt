package org.ifmo.isbdcurs.util

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class Coordinate(val lat: Double, val lon: Double);

fun Coordinate.distance(other: Coordinate): Double {
    val theta = this.lon - other.lon
    var dist = sin(deg2rad(this.lat)) * sin(deg2rad(other.lat)) + cos(deg2rad(this.lat)) * cos(deg2rad(other.lat)) * cos(deg2rad(theta))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515
    return dist
}

private fun deg2rad(deg: Double): Double {
    return deg * Math.PI / 180.0
}

private fun rad2deg(rad: Double): Double {
    return rad * 180.0 / Math.PI
}
