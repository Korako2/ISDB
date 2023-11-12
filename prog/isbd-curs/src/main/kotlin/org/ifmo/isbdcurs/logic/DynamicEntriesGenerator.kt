package org.ifmo.isbdcurs.logic

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.util.Coordinate
import org.ifmo.isbdcurs.util.distance
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val random = Random(42)

data class TimePeriod(val start: Instant, val end: Instant);

data class TransferTimePattern(val start: Instant, val increment: Duration, val noiseHoursMax: Double);

fun TransferTimePattern.calculatePointInTime(stepIndex: Int): Instant {
    if (this.noiseHoursMax == 0.0) {
        return this.start.plus(this.increment.times(stepIndex))
    }
    val noiseHoursDelta = random.nextDouble(-this.noiseHoursMax, +this.noiseHoursMax).hours + random.nextDouble(-20.0, +20.0).minutes
    val totalIncrement = this.increment.times(stepIndex)
    return this.start.plus(totalIncrement).plus(noiseHoursDelta)
}

fun DriverStatusHistory.generateSeriesFromFirst(
    intervalHours: Duration, noiseHoursMax: Double
): List<DriverStatusHistory> {
    val driverId = this.driverId
    val startDate = this.date.toKotlinInstant()

    val allStatuses = DriverStatus.values()
    val timePattern = TransferTimePattern(startDate, intervalHours, noiseHoursMax)
    return mutableListOf(this) + allStatuses.slice(1 until allStatuses.size).mapIndexed { i, status ->
        DriverStatusHistory(driverId, timePattern.calculatePointInTime(i + 1).toJavaInstant(), status)
    }
}

class DynamicEntriesGenerator(
    private val actionsPeriod: TimePeriod,
    private val transferTimePattern: TransferTimePattern,
) {

    fun generateSeriesBetweenPoints(a: Coordinate, b: Coordinate, points: Int, noiseMax: Float): List<Coordinate> {
        val noise = random.nextDouble(-noiseMax.toDouble(), +noiseMax.toDouble()).toFloat()
        val xStep = (b.lat - a.lat) / (points - 1) + noise
        val yStep = (b.lon - a.lon) / (points - 1) + noise
        return (0 until points).map { i -> Coordinate(a.lat + i * xStep, a.lon + i * yStep) }
    }

    private fun actionsPeriodNoised(randIndex: Long): TimePeriod {
        val noise = random.nextLong(1, 120).days +
                random.nextLong(0, 24).hours +
                random.nextLong(0, 60).minutes +
                random.nextLong(0, 60).seconds +
                (randIndex % 999L).milliseconds
        return TimePeriod(actionsPeriod.start.plus(noise), actionsPeriod.end.minus(noise));
    }

    fun generateMovementHistory(
        vehicleId: Long,
        previousHistory: MutableList<VehicleMovementHistory>,
        a: Coordinate,
        b: Coordinate,
        newDriverHistory: List<DriverStatusHistory>
    ) {
        val pointsCount = newDriverHistory.size;
        if (pointsCount == 0) {
            return
        }
        val coordinates = generateSeriesBetweenPoints(a, b, pointsCount, 0.3f)
        // get last mileage from previousHistory and increment it on each new item in movement history
        var lastMileage = previousHistory.lastOrNull()?.mileage ?: 0.0f
        var lastCoordinate = previousHistory.lastOrNull()?.let { Coordinate(it.latitude, it.longitude) } ?: a
        val extraHistory = newDriverHistory.zip(coordinates).map { (status, point) ->
            if (point != lastCoordinate)
                lastMileage += point.distance(lastCoordinate)
            lastCoordinate = point
            VehicleMovementHistory(
                vehicleId,
                status.date,
                latitude = point.lat,
                longitude = point.lon,
                mileage = lastMileage
            )
        }
        previousHistory.addAll(extraHistory)
    }

    fun genDriverStatusesHistory(driverId: Long, dayOffset: Long): List<DriverStatusHistory> {
        val initialStatus = DriverStatusHistory(driverId, actionsPeriodNoised(dayOffset).start.plus((dayOffset % 9000L).days).toJavaInstant(), DriverStatus.OFF_DUTY)
        return initialStatus.generateSeriesFromFirst(
            transferTimePattern.increment, transferTimePattern.noiseHoursMax
        )
    }

    fun genOrderStatuses(orderId: Long, driverHistory: List<DriverStatusHistory>): List<OrderStatuses> {
        val driverStatusToOrderStatus = mapOf(
            DriverStatus.ACCEPTED_ORDER to OrderStatus.ACCEPTED,
            DriverStatus.ARRIVED_AT_LOADING_LOCATION to OrderStatus.ARRIVED_AT_LOADING_LOCATION,
            DriverStatus.LOADING to OrderStatus.LOADING,
            DriverStatus.EN_ROUTE to OrderStatus.ON_THE_WAY,
            DriverStatus.ARRIVED_AT_UNLOADING_LOCATION to OrderStatus.ARRIVED_AT_UNLOADING_LOCATION,
            DriverStatus.UNLOADING to OrderStatus.UNLOADING,
            DriverStatus.COMPLETED_ORDER to OrderStatus.COMPLETED,
        )
        return driverHistory.filter { d -> driverStatusToOrderStatus.keys.contains(d.status) }
            .map { d ->
                OrderStatuses(
                    orderId=orderId,
                    d.date,
                    driverStatusToOrderStatus[d.status]!!
                )
            }
    }

    fun genFuelExpenses(fuelCardNumber: String, distance: Float): FuelExpenses {
        return FuelExpenses(fuelCardNumber, actionsPeriodNoised(distance.toLong()).end.toJavaInstant(), distance * 4.0f)
    }
}
