package org.ifmo.isbdcurs.generator

import kotlinx.datetime.Instant
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.util.Coordinate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

data class AllTables(
    val persons: List<Person>,
    val contactInfos: List<ContactInfo>,
    val drivers: List<Driver>,
    val customers: List<Customer>,
    val driverStatusHistory: List<DriverStatusHistory>,
    val tariffRates: List<TariffRate>,
    val driverLicenses: List<DriverLicense>,
    val vehicles: List<Vehicle>,
    val vehicleOwnerships: List<VehicleOwnership>,
    val vehicleMovementHistory: List<VehicleMovementHistory>,
    val orders: List<Order>,
    val orderStatuses: List<OrderStatuses>,
    val cargos: List<Cargo>,
    val addresses: List<Address>,
    val storagePoints: List<StoragePoint>,
    val loadingUnloadingAgreements: List<LoadingUnloadingAgreement>,
    val fuelCardsForDrivers: List<FuelCardsForDrivers>,
    val fuelExpenses: List<FuelExpenses>,
)

data class OrderPack(
    var order: Order,
    var cargo: Cargo,
    val driverPack: DriverPack,
)

data class DriverPack(
    val driver: Driver,
    val vehicle: Vehicle,
    val movementHistory: MutableList<VehicleMovementHistory>
)

class FillTables {
    val logger: Logger = LoggerFactory.getLogger(FillTables::class.java)
    companion object {
        private val random = Random(42)

        private val largePeriod = TimePeriod(
            Instant.parse("1990-01-01T00:00:00Z"),
            Instant.parse("2040-01-01T00:00:00Z"),
        )
        private val actionsPeriod = TimePeriod(
            Instant.parse("2013-01-01T00:00:00Z"),
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
                    increment = 2.5.hours,
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

    fun createContactInfos(driversCount: Int, customersCount: Int) : List<ContactInfo> {
        val ordersCount = customersCount * 100
        val addressesCount = customersCount * 10
        val personsCount = driversCount + customersCount;

        val persons = (1L..personsCount).map { i -> staticEntriesGenerator.genPerson().apply { id = i } }

        return contactInfos(persons)
    }

    private fun contactInfos(persons: List<Person>): List<ContactInfo> {
        val contactInfos = persons.map { staticEntriesGenerator.genContactInfoMail(it.id!!) }.toMutableList()
        contactInfos += contactInfos.map { staticEntriesGenerator.genContactInfoPhone(it.personId) }
        return contactInfos
    }

    fun createData(driversCount: Int, customersCount: Int): AllTables {
        val ordersCount = customersCount * 100
        val addressesCount = customersCount * 10
        val personsCount = driversCount + customersCount;

        val persons = (1L..personsCount).map { i -> staticEntriesGenerator.genPerson().apply { id = i } }

        val drivers = (1L..driversCount).map { i ->
            staticEntriesGenerator.genDriver(personId = persons.random(random).id!!).apply { id = i }
        }
        val fuelCards = drivers.map { staticEntriesGenerator.genFuelCardsForDrivers(it.id!!) }
        val licenses = drivers.map { staticEntriesGenerator.genDriverLicense(it.id!!) }
        val tariffRates = drivers.map { staticEntriesGenerator.genTariffRate(it.id!!) }
        val contactInfos = contactInfos(persons)
        val addresses = (1L..addressesCount).map { staticEntriesGenerator.genAddress().apply { id = it } }
        val storagePoints = addresses.map { staticEntriesGenerator.genStoragePoint(it.id!!) }
        val customers = (1L..customersCount).map { i ->
            staticEntriesGenerator.genCustomer(persons.random(random).id!!).apply { id = i }
        }

        // create drivers
        val driverPacks = (1..driversCount).map {
            val driver =
                staticEntriesGenerator.genDriver(personId = persons.random(random).id!!).apply { id = it.toLong() }
            val vehicle = staticEntriesGenerator.genVehicle().apply { id = it.toLong() }
            DriverPack(driver, vehicle, mutableListOf())
        }
        // orders
        val orderPacks = (1L..ordersCount).map { orderId ->
            val driverPack = driverPacks.random(random)
            val order = staticEntriesGenerator.genOrder(
                customerId = customers.random(random).id!!,
                vehicleId = driverPack.vehicle.id!!,
            ).apply { id = orderId }
            val cargo = staticEntriesGenerator.genCargo(orderId = order.id!!).apply { id = order.id }
            OrderPack(order, cargo, driverPack)
        }

        val ownerships = driverPacks.map { p ->
            staticEntriesGenerator.genOwnerShip(
                vehicleId = p.vehicle.id!!, driverId = p.driver.id!!
            )
        };

        val orderToPointsCache : MutableMap<Int, Pair<Int, Int>> = mutableMapOf()

        val agreements = orderPacks.mapIndexed { i, p ->
            var addrAIndex = random.nextInt(addresses.size)
            val addrBIndex = random.nextInt(addresses.size - 1)
            if (addrBIndex == addrAIndex)
                addrAIndex += 1
            val addressA = addresses[addrAIndex]
            val addressB = addresses[addrBIndex]
            orderToPointsCache[i] = Pair(addrAIndex, addrBIndex)
            staticEntriesGenerator.genLoadingUnloadingAgreement(
                orderId = p.order.id!!,
                driverId = p.driverPack.driver.id!!,
                departurePoint = addressA.id!!,
                deliveryPoint = addressB.id!!,
                senderId = customers.random(random).id!!,
                receiverId = customers.random(random).id!!,
            )
        }

        // dynamic:
        val dynamicGen = dynamicGenerators.random(random)

        val fuelCardExpensesCounter = mutableMapOf<String, Int>()
        val allExpenses = orderPacks.map { p ->
            val fuelCard = fuelCards.first { it.driverId == p.driverPack.driver.id }
            val distance = p.order.distance
            val cardNumber = fuelCard.fuelCardNumber
            fuelCardExpensesCounter[cardNumber] = fuelCardExpensesCounter.getOrDefault(cardNumber, 0) + 1
            val expenses = dynamicGen.genFuelExpenses(cardNumber, distance, fuelCardExpensesCounter[cardNumber]!!)
            expenses
        }

        val driverOrdersCounter = mutableMapOf<Long, Int>()
        val statusHistories = orderPacks.map { op ->
            val dp = op.driverPack
            val driverId = dp.driver.id!!
            driverOrdersCounter[driverId] = driverOrdersCounter.getOrDefault(driverId, 0) + 1
            val driverHistory = dynamicGen.genDriverStatusesHistory(driverId, orderForDriver = driverOrdersCounter[driverId]!!)
            val orderHistory = dynamicGen.genOrderStatuses(op.order.id!!, driverHistory)
            Pair(driverHistory, orderHistory)
        }
        val orderStatusHistory = statusHistories.map { it.second }.flatten()
        val driverStatusHistory = statusHistories.map { it.first }.flatten()

        orderPacks.mapIndexed { i, op ->
            val startPoint = storagePoints[orderToPointsCache[i]!!.first]
            val endPoint = storagePoints[orderToPointsCache[i]!!.second]
            val cord1 = Coordinate(startPoint.latitude, startPoint.longitude)
            val cord2 = Coordinate(endPoint.latitude, endPoint.longitude)
            val orderHistory = statusHistories[i]
            val driverHistory = orderHistory.first
            dynamicGen.generateMovementHistory(vehicleId=op.driverPack.vehicle.id!!, op.driverPack.movementHistory, cord1, cord2, driverHistory)
        }
        val mh = driverPacks.map { it.movementHistory }.flatten()

        val vehicles = driverPacks.map { it.vehicle }
        val orders = orderPacks.map { it.order }
        val cargos = orderPacks.map { it.cargo }

        return AllTables(
            persons,
            contactInfos,
            drivers,
            customers,
            driverStatusHistory,
            tariffRates,
            licenses,
            vehicles,
            ownerships,
            mh,
            orders,
            orderStatusHistory,
            cargos,
            addresses,
            storagePoints,
            agreements,
            fuelCards,
            allExpenses
        )
    }
}
