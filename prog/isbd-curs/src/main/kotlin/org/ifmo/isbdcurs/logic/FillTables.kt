package org.ifmo.isbdcurs.logic

import kotlinx.datetime.Instant
import org.ifmo.isbdcurs.persistence.*
import org.ifmo.isbdcurs.util.Coordinate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours

@Service
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

    @Autowired
    lateinit var repository: PersonRepository

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var contactInfoRepository: ContactInfoRepository

    @Autowired
    lateinit var driverRepository: DriverRepository

    @Autowired
    lateinit var customerRepository: CustomerRepository

    @Autowired
    lateinit var driverStatusHistoryRepository: DriverStatusHistoryRepository

    @Autowired
    lateinit var tariffRateRepository: TariffRateRepository

    @Autowired
    lateinit var driverLicenseRepository: DriverLicenseRepository

    @Autowired
    lateinit var vehicleRepository: VehicleRepository

    @Autowired
    lateinit var vehicleOwnershipRepository: VehicleOwnershipRepository

    @Autowired
    lateinit var vehicleMovementHistoryRepository: VehicleMovementHistoryRepository

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var orderStatusesRepository: OrderStatusesRepository

    @Autowired
    lateinit var cargoRepository: CargoRepository

    @Autowired
    lateinit var addressRepository: AddressRepository

    @Autowired
    lateinit var storagePointRepository: StoragePointRepository

    @Autowired
    lateinit var loadingUnloadingAgreementRepository: LoadingUnloadingAgreementRepository

    @Autowired
    lateinit var fuelCardsForDriversRepository: FuelCardsForDriversRepository

    @Autowired
    lateinit var fuelExpensesRepository: FuelExpensesRepository

    fun fill() {
        // static:
        val driversCount = 50L
        val customersCount = 50L
        val addressesCount = 10L

        val personsCount = driversCount + customersCount;
        val persons = (1..personsCount).map { staticEntriesGenerator.genPerson() }
        personRepository.saveAll(persons)
        val drivers = (1..driversCount).map { staticEntriesGenerator.genDriver(persons.random().id!!) }
        driverRepository.saveAll(drivers)
        val driversWithOrders = drivers.filter { random.nextBoolean() }
        val ordersCount = driversWithOrders.size
        val getDriverWithOrder = { driversWithOrders.random().id!! }

        val customers = (1..customersCount).map { staticEntriesGenerator.genCustomer(persons.random().id!!) }
        customerRepository.saveAll(customers)
        val vehicles = (1..driversCount).map { staticEntriesGenerator.genVehicle() }
        vehicleRepository.saveAll(vehicles)

        val vehicleToDriver = vehicles.zip(driversWithOrders.shuffled())

        val orders = (1..ordersCount).zip(vehicleToDriver).map {(o, v) ->
            staticEntriesGenerator.genOrder(
                customers.random().id!!, v.first.id!!
            )
        }
        orderRepository.saveAll(orders)
        val vehicleToOrder = vehicleToDriver.map { it.first }.zip(orders)

        val fuelCards = drivers.map { staticEntriesGenerator.genFuelCardsForDrivers(it.id!!) }
        fuelCardsForDriversRepository.saveAll(fuelCards)
        val licenses = drivers.map { staticEntriesGenerator.genDriverLicense(it.id!!) }
        driverLicenseRepository.saveAll(licenses)
        val tariffRates = drivers.map { staticEntriesGenerator.genTariffRate(it.id!!) }
        tariffRateRepository.saveAll(tariffRates)
        val contactInfos = persons.map { staticEntriesGenerator.genContactInfo(it.id!!) }
        contactInfoRepository.saveAll(contactInfos)
        val ownerships = vehicleToDriver.map { (v, d) -> staticEntriesGenerator.genOwnerShip(vehicleId = v.id!!, driverId = d.id!!) }
        vehicleOwnershipRepository.saveAll(ownerships)
        val cargos = orders.map { staticEntriesGenerator.genCargo(orderId = it.id!!) }
        cargoRepository.saveAll(cargos)
        val addresses = (1..addressesCount).map { staticEntriesGenerator.genAddress() }
        addressRepository.saveAll(addresses)
        val storagePoints = addresses.map { staticEntriesGenerator.genStoragePoint(it.id!!) }
        storagePointRepository.saveAll(storagePoints)

        val agreements = orders.map {
            val addressA = addresses.random()
            staticEntriesGenerator.genLoadingUnloadingAgreement(
                it.id!!, getDriverWithOrder(),
                departurePoint = addressA.id!!,
                deliveryPoint = addresses.minus(addressA).random().id!!,
                senderId = customers.random().id!!,
                receiverId = customers.random().id!!,
            )
        }
        loadingUnloadingAgreementRepository.saveAll(agreements)

        // dynamic:
        val dynamicGen = dynamicGenerators.random()
        fuelCards.map { fuelCard ->
            fuelCardsForDriversRepository.findByFuelCardNumber(fuelCard.fuelCardNumber)!!.driverId.let {
                orderRepository.findByVehicleId(it).lastOrNull()?.distance?.let { distance ->
                    val expenses = dynamicGen.genFuelExpenses(fuelCard.fuelCardNumber, distance)
                    fuelExpensesRepository.save(expenses)
                }
            }
        }

        val driverStatusHistory = driversWithOrders.map { dynamicGen.genDriverStatusesHistory(it.id!!) }
        driverStatusHistory.map { statusHistoryList ->
            driverStatusHistoryRepository.saveAll(statusHistoryList)
        }

        vehicleToOrder.map {(v, o) ->
            val agreement = agreements.first { it.orderId == o.id }
            val startPoint = storagePoints.first { it.addressId == agreement.departurePoint }
            val endPoint = storagePoints.first { it.addressId == agreement.deliveryPoint }
            val cord1 = Coordinate(startPoint.latitude, startPoint.longitude)
            val cord2 = Coordinate(endPoint.latitude, endPoint.longitude)
            val driverId = agreement.driverId
            val driverHistory = driverStatusHistory.first { it.first().driverId == driverId }
            val mh = dynamicGen.generateMovementHistory(v.id!!, listOf(), cord1, cord2, driverHistory)
            vehicleMovementHistoryRepository.saveAll(mh)
        }
    }
}