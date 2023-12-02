package org.ifmo.isbdcurs.util

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

data class Coordinate(val lat: Float, val lon: Float);

fun Coordinate.distance(other: Coordinate): Float {
    val theta = this.lon - other.lon
    var dist = sin(deg2rad(this.lat)) * sin(deg2rad(other.lat)) + cos(deg2rad(this.lat)) * cos(deg2rad(other.lat)) * cos(deg2rad(theta))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515f
    return dist
}

private fun deg2rad(deg: Float): Float {
    return (deg * Math.PI / 180.0f).toFloat()
}

private fun rad2deg(rad: Float): Float {
    return (rad * 180.0f / Math.PI).toFloat()
}
