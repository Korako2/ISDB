package org.ifmo.isbdcurs.logic

import kotlinx.datetime.Instant
import org.ifmo.isbdcurs.models.DriverStatus
import org.ifmo.isbdcurs.models.DriverStatusHistory
import org.ifmo.isbdcurs.models.FuelExpenses
import org.ifmo.isbdcurs.models.VehicleMovementHistory
import org.ifmo.isbdcurs.util.Coordinate
import org.ifmo.isbdcurs.util.distance
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours


data class TimePeriod(val start: Instant, val end: Instant);

data class TransferTimePattern(val start: Instant, val increment: Duration, val noiseHoursMax: Double);

fun TransferTimePattern.calculatePointInTime(stepIndex: Int): Instant {
    val noiseHoursDelta = Random.nextDouble(-this.noiseHoursMax, +this.noiseHoursMax).hours
    val totalIncrement = this.increment.times(stepIndex)
    return this.start.plus(totalIncrement).plus(noiseHoursDelta)
}

fun DriverStatusHistory.generateSeriesFromFirst(
    intervalHours: Duration, noiseHoursMax: Double
): List<DriverStatusHistory> {
    val driverId = this.driverId
    val startDate = this.date

    val allStatuses = DriverStatus.values()
    val timePattern = TransferTimePattern(startDate, intervalHours, noiseHoursMax)
    return mutableListOf(this) + allStatuses.slice(1 until allStatuses.size).mapIndexed { i, status ->
        DriverStatusHistory(driverId, timePattern.calculatePointInTime(i + 1), status)
    }
}

class DynamicEntriesGenerator(
    private val actionsPeriod: TimePeriod,
    private val transferTimePattern: TransferTimePattern,
) {
    companion object {
        fun generateSeriesBetweenPoints(a: Coordinate, b: Coordinate, points: Int, noiseMax: Double): List<Coordinate> {
            val noise = Random.nextDouble(-noiseMax, +noiseMax);
            val xStep = (b.lat - a.lat) / (points - 1) + noise
            val yStep = (b.lon - a.lon) / (points - 1) + noise
            return (0 until points).map { i -> Coordinate(a.lat + i * xStep, a.lon + i * yStep) }
        }
    }

    private fun actionsPeriodNoised(): TimePeriod {
        val noise = Random.nextLong(5, 60).days;
        return TimePeriod(actionsPeriod.start.plus(noise), actionsPeriod.end.minus(noise));
    }

    fun generateMovementHistory(
        vehicleId: Long,
        previousHistory: List<VehicleMovementHistory>,
        a: Coordinate,
        b: Coordinate,
        driverHistory: List<DriverStatusHistory>
    ): List<VehicleMovementHistory> {
        val pointsCount = driverHistory.size;
        if (pointsCount == 0) {
            return previousHistory
        }
        val coordinates = generateSeriesBetweenPoints(a, b, pointsCount, 0.3)
        // get last mileage from previousHistory and increment it on each new item in movement history
        var lastMileage = previousHistory.lastOrNull()?.mileage ?: 0.0
        var lastCoordinate = previousHistory.lastOrNull()?.let { Coordinate(it.latitude, it.longitude) } ?: a
        return previousHistory + driverHistory.zip(coordinates).map { (status, point) ->
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
    }

    fun genOrderStatuses(): List<DriverStatusHistory> {
        val initialStatus = DriverStatusHistory(1L, actionsPeriod.start, DriverStatus.OFF_DUTY)
        return initialStatus.generateSeriesFromFirst(
            transferTimePattern.increment, transferTimePattern.noiseHoursMax
        )
    }

    fun genFuelExpenses(fuelCardNumberId: Long, distance: Double): FuelExpenses {
        return FuelExpenses(fuelCardNumberId, actionsPeriodNoised().end, distance * 4.0)
    }
}

class SingleRouteGenerator(
    private val largePeriod: TimePeriod,
    private val actionsPeriod: TimePeriod,
    private val transferTimePattern: TransferTimePattern,
) {
    fun generateRoute(): List<DriverStatusHistory> {
        val initialStatus = DriverStatusHistory(1L, largePeriod.start, DriverStatus.OFF_DUTY)
        return initialStatus.generateSeriesFromFirst(
            transferTimePattern.increment, transferTimePattern.noiseHoursMax
        )
    }
}