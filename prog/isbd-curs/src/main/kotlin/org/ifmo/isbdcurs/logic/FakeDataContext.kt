package org.ifmo.isbdcurs.logic

import io.github.serpro69.kfaker.faker
import kotlinx.datetime.Instant
import org.ifmo.isbdcurs.models.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime

data class TimePeriod(val start: Instant, val end: Instant);

data class TransferTimePattern(val start: Instant, val increment: Duration, val noiseHoursMax: Double);

data class Point(val x: Double, val y: Double);

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
    //  add the initial status to the start of the list
    return mutableListOf(this) + allStatuses.slice(1 until allStatuses.size).mapIndexed { i, status ->
        DriverStatusHistory(driverId, timePattern.calculatePointInTime(i + 1), status)
    }
}

// function to generate series of (x,y) points between point A and point B with noise
// distance between each point should be a little bit different
fun generateSeriesBetweenPoints(a: Point, b: Point, stepCount: Int, noiseMax: Double): List<Point> {
    val noise = Random.nextDouble(-noiseMax, +noiseMax);
    val xStep = (b.x - a.x) / stepCount + noise
    val yStep = (b.y - a.y) / stepCount + noise
    return (0..stepCount).map { i -> Point(a.x + i * xStep, a.x + i * yStep) }
}

class StaticEntitiesGenerator(
    private val largePeriod: TimePeriod,
    private val actionsPeriod: TimePeriod,
    private val transferTimePattern: TransferTimePattern,
) {
    private val faker = faker {}

    private fun largePeriodNoised(): TimePeriod {
        val noise = Random.nextLong(0, 365).days * 5;
        return TimePeriod(largePeriod.start.plus(noise), largePeriod.end.minus(noise));
    }

    private fun actionsPeriodNoised(): TimePeriod {
        val noise = Random.nextLong(5, 60).days;
        return TimePeriod(actionsPeriod.start.plus(noise), actionsPeriod.end.minus(noise));
    }

    fun genPerson(): Person {
        return Person(-1L, faker.name.firstName(), faker.name.lastName(), null)
    }

    fun genContactInfo(personId: Long): ContactInfo {
        return ContactInfo(personId, ContactInfoType.EMAIL, faker.internet.email())
    }

    fun genDriver(personId: Long): Driver {
        return Driver(-1L, personId, faker.finance.creditCard("visa"))
    }

    fun genCustomer(personId: Long): Customer {
        return Customer(-1L, personId, faker.company.name())
    }

    fun genTariffRate(driverId: Long): TariffRate {
        return TariffRate(driverId, Random.nextInt(50, 100), Random.nextInt(5, 10))
    }

    fun genDriverLicense(driverId: Long): DriverLicense {
        return DriverLicense(
            driverId, largePeriodNoised().start, largePeriodNoised().end, Random.nextInt(100000, 999999)
        )
    }

    fun genVehicle(): Vehicle {
        val plateNumber = faker.string.regexify("""[А-Я]{1}\d{3}[А-Я]{2}\d{2}""")
        return Vehicle(
            -1L,
            plateNumber,
            model = faker.vehicle.modelsByMake(""),
            manufactureYear = largePeriod.start,
            length = Random.nextDouble(12.0, 15.0),
            width = Random.nextDouble(1.5, 2.5),
            height = Random.nextDouble(1.5, 4.0),
            loadCapacity = Random.nextDouble(1.0, 3.0),
            bodyType = BodyType.values().random()
        )
    }

    fun genOwnerShip(vehicleId: Long, driverId: Long): VehicleOwnership {
        return VehicleOwnership(vehicleId, driverId, largePeriodNoised().start, largePeriodNoised().end)
    }

    fun genOrder(customerId: Long, vehicleId: Long): Order {
        return Order(
            -1L,
            customerId = customerId,
            distance = Random.nextDouble(30.0, 1000.0),
            price = Random.nextDouble(1000.0, 100000.0),
            orderDate = actionsPeriodNoised().start,
            vehicleId = vehicleId,
        )
    }
}

// dynamic:
// movementHistory
// orderStatuses
// driverStatuses
// FuelExpenses

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