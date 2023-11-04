package org.ifmo.isbdcurs.logic

import io.github.serpro69.kfaker.faker
import kotlinx.datetime.LocalTime
import org.ifmo.isbdcurs.models.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class StaticEntitiesGenerator(
    private val largePeriod: TimePeriod,
    private val actionsPeriod: TimePeriod,
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

    fun genCargo(orderId: Long): Cargo {
        return Cargo(
            -1L,
            weight = Random.nextDouble(1.0, 100.0),
            width = Random.nextDouble(0.4, 2.5),
            height = Random.nextDouble(0.4, 4.0),
            length = Random.nextDouble(0.4, 15.0),
            orderId = orderId,
            cargoType = CargoType.values().random(),
        )
    }

    fun genAddress(): Address {
        return Address(
            -1L,
            country = "Thailand",
            city = faker.address.city(),
            street = faker.address.streetName(),
            building = Random.nextInt(1, 100),
            corpus = Random.nextInt(1, 10),
        )
    }

    fun genStoragePoint(addressId: Long): StoragePoint {
        return StoragePoint(
            addressId,
            latitude = Random.nextDouble(14.0, 17.0),
            longitude = Random.nextDouble(98.0, 103.0),
        )
    }

    fun genLoadingUnloadingAgreement(
        orderId: Long, driverId: Long, departurePoint: Long, deliveryPoint: Long, senderId: Long, receiverId: Long
    ): LoadingUnloadingAgreement {
        return LoadingUnloadingAgreement(
            orderId,
            driverId,
            departurePoint,
            deliveryPoint,
            senderId,
            receiverId,
            unloadingTime = LocalTime(Random.nextInt(1, 12), 0),
            loadingTime = LocalTime(Random.nextInt(1, 12), 0),
        )
    }

    fun genFuelCardsForDrivers(driverId: Long): FuelCardsForDrivers {
        return FuelCardsForDrivers(
            driverId,
            fuelCardNumber = faker.string.numerify("################"),
            fuelStationName = faker.company.buzzwords() + " fuel station",
        )
    }
}
