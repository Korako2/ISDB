package org.ifmo.isbdcurs.logic

import org.ifmo.isbdcurs.models.DriverStatus
import org.ifmo.isbdcurs.models.DriverStatusHistory
import java.time.Duration
import java.time.Instant

data class TimePeriod(val start: Instant, val end: Instant);

data class TransferTimePattern(val start: Instant, val increment: Duration, val noiseHoursMax: Double);

data class Point(val x: Double, val y: Double);

fun randomModuloOne(): Double {
    return (Math.random() - 0.5) * 2  // [-1, 1]
}

fun TransferTimePattern.calculatePointInTime(stepIndex: Int): Instant {
    val delta = randomModuloOne()
    val noiseHoursDelta = Duration.ofHours((delta * this.noiseHoursMax).toLong())  // [-1 * x, 1 * x]
    val totalIncrement = this.increment.multipliedBy(stepIndex.toLong())
    return this.start.plus(totalIncrement).plus(noiseHoursDelta)
}

fun DriverStatusHistory.generateSeriesFromFirst(intervalHours: Long, noiseHoursMax: Double): List<DriverStatusHistory> {
    val driverId = this.driverId
    val startDate = this.date

    val allStatuses = DriverStatus.values()
    val timePattern = TransferTimePattern(startDate, Duration.ofHours(intervalHours), noiseHoursMax)
    //  add the initial status to the start of the list
    return mutableListOf(this) + allStatuses.slice(1 until allStatuses.size).mapIndexed { i, status ->
            DriverStatusHistory(driverId, timePattern.calculatePointInTime(i+1), status)
        }
}

// function to generate series of (x,y) points between point A and point B with noise
// distance between each point should be a little bit different
fun generateSeriesBetweenPoints(a: Point, b: Point, stepCount: Int, noise: Float): List<Point> {
    val delta = randomModuloOne();
    val xStep = (b.x - a.x) / stepCount + delta * noise
    val yStep = (b.y - a.y) / stepCount + delta * noise
    return (0..stepCount).map { i -> Point(a.x + i * xStep, a.x + i * yStep) }
}

/*
* This class is responsible for creating and filling entities with random data
 */
// it should store date range as a fields
class FakeDataContext(
    // constraint for all dates
    private val largePeriod: TimePeriod,
    // constraint time interval for actions such as transfer the cargo
    private val actionsPeriod: TimePeriod,
    private val transferTimePattern: TransferTimePattern,
) {

}