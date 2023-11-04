package org.ifmo.isbdcurs.logic

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.ifmo.isbdcurs.models.DriverStatus
import org.ifmo.isbdcurs.models.DriverStatusHistory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours

class FakeDataContextTest() {
    // test function generateSeriesFromFirst
    @Test
    fun testGenerateSeriesFromFirst() {
        // we create initial History status
        // all other statuses should be generated from this one
        val intervalHours = 1L
        val noiseHoursMax = 0.0
        val initialDateTime = LocalDateTime.parse("2020-01-01T00:00")
        val initialStatus = DriverStatusHistory(1L, initialDateTime.toInstant(TimeZone.UTC), DriverStatus.OFF_DUTY)
        val driverStatusHistorySeries = initialStatus.generateSeriesFromFirst(intervalHours.hours, noiseHoursMax)
        val expectedLastElement = DriverStatusHistory(
            1L, LocalDateTime.parse("2020-01-01T07:00").toInstant(TimeZone.UTC), DriverStatus.COMPLETED_ORDER
        )
        assertEquals(driverStatusHistorySeries[0], initialStatus)
        assertArrayEquals(driverStatusHistorySeries.map { it.status }.toTypedArray(), DriverStatus.values())
        assertEquals(driverStatusHistorySeries.last(), expectedLastElement)
        assertEquals(driverStatusHistorySeries.size, DriverStatus.values().size)
    }

    @Test
    fun generateSeriesBetweenPoints() {
        val a = Point(0.0, 0.0)
        val b = Point(1.0, 1.0)
        val stepCount = 10
        val noise = 0.0
        val series = generateSeriesBetweenPoints(a, b, stepCount, noise)
        assertEquals(series.size, stepCount + 1)
        assertEquals(series[0], a)
        assertEquals(series[stepCount], b)
    }
}