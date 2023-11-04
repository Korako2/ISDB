package org.ifmo.isbdcurs.logic

import kotlinx.datetime.Instant
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

class FillTables {
    companion object {
        private val random = Random(42)

        private val largePeriod = TimePeriod(
            Instant.parse("1990-01-01T00:00:00Z"),
            Instant.parse("2040-01-01T00:00:00Z"),
        )
        private val actionsPeriod = TimePeriod(
            Instant.parse("2023-01-01T00:00:00Z"),
            Instant.parse("2023-12-31T00:00:00Z"),
        )
        private val transferPatternStartDates = listOf(
            Instant.parse("2023-01-01T00:00:00Z"),
            Instant.parse("2023-04-01T00:00:00Z"),
            Instant.parse("2023-08-01T00:00:00Z"),
        )

        private val staticEntriesGenerator = StaticEntitiesGenerator(
            largePeriod,
            actionsPeriod,
            random,
        )

        private val dynamicGenerators = createDynamicEntriesGenerators()

        private fun createDynamicEntriesGenerators(): List<DynamicEntriesGenerator> {
            return transferPatternStartDates.map {
                TransferTimePattern(
                    it,
                    increment = 4.hours,
                    noiseHoursMax = 0.5,
                )
            }.map {
                DynamicEntriesGenerator(
                    actionsPeriod,
                    it,
                )
            }
        }
    }

    fun fill() {
        // static:
        val driversCount = 100L
        val customersCount = 50L
        val addressesCount = 10L
        val personsCount = driversCount + customersCount;
        val persons = (1..personsCount).map { staticEntriesGenerator.genPerson(it) }
        val drivers = (1..driversCount).map { staticEntriesGenerator.genDriver(it, persons.random().id!!) }
        val customers = (1..customersCount).map { staticEntriesGenerator.genCustomer(it, persons.random().id!!) }
        val vehicles = (1..driversCount).map { staticEntriesGenerator.genVehicle(it) }
        val orders = (1..customersCount).map {
            staticEntriesGenerator.genOrder(
                it, customers.random().id!!, vehicles.random().id!!
            )
        }

        val fuelCards = drivers.map { staticEntriesGenerator.genFuelCardsForDrivers(it.id!!) }
        val licenses = drivers.map { staticEntriesGenerator.genDriverLicense(it.id!!) }
        val tariffRates = drivers.map { staticEntriesGenerator.genTariffRate(it.id!!) }
        val contactInfos = persons.map { staticEntriesGenerator.genContactInfo(it.id!!) }
        val ownerships = vehicles.map { staticEntriesGenerator.genOwnerShip(vehicleId = it.id!!, driverId = it.id!!) }
        val cargos = orders.map { staticEntriesGenerator.genCargo(it.id!!, orderId = it.id!!) }
        val addresses = (1..addressesCount).map { staticEntriesGenerator.genAddress(it) }
        val storagePoints = addresses.map { staticEntriesGenerator.genStoragePoint(it.id!!) }

        val agreements = orders.map {
            val addressA = addresses.random()
            staticEntriesGenerator.genLoadingUnloadingAgreement(
                it.id!!, drivers.random().id!!,
                departurePoint = addressA.id!!,
                deliveryPoint = addresses.minus(addressA).random().id!!,
                senderId = customers.random().id!!,
                receiverId = customers.random().id!!,
            )
        }
    }
}