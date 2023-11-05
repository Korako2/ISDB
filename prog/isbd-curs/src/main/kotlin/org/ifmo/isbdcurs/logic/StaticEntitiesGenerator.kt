package org.ifmo.isbdcurs.logic

import io.github.serpro69.kfaker.faker
import kotlinx.datetime.*
import org.ifmo.isbdcurs.models.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class StaticEntitiesGenerator(
    private val largePeriod: TimePeriod,
    private val actionsPeriod: TimePeriod,
    private val random: Random
) {
    private val faker = faker {}

    private fun largePeriodNoised(): TimePeriod {
        val noise = random.nextLong(0, 365).days * 5;
        return TimePeriod(largePeriod.start.plus(noise), largePeriod.end.minus(noise));
    }

    private fun actionsPeriodNoised(): TimePeriod {
        val noise = random.nextLong(5, 60).days;
        return TimePeriod(actionsPeriod.start.plus(noise), actionsPeriod.end.minus(noise));
    }

    fun genPerson(): Person {
        val gender = if (random.nextBoolean()) 'M' else 'F'
        val birthDate = LocalDate(largePeriodNoised().start.toLocalDateTime(TimeZone.UTC).year, 1, 1)
        return Person(null, faker.name.firstName(), faker.name.lastName(), null, gender, birthDate.toJavaLocalDate())
    }

    fun genContactInfo(personId: Long): ContactInfo {
        return ContactInfo(personId, ContactInfoType.EMAIL, faker.internet.email())
    }

    fun genDriver(personId: Long): Driver {
        val passport = faker.string.regexify("""\d{10}""")
        return Driver(null, personId, passport, faker.finance.creditCard("visa"))
    }

    fun genCustomer(personId: Long): Customer {
        return Customer(null, personId, faker.company.name())
    }

    fun genTariffRate(driverId: Long): TariffRate {
        return TariffRate(driverId, random.nextInt(50, 100), random.nextInt(5, 10))
    }

    fun genDriverLicense(driverId: Long): DriverLicense {
        return DriverLicense(
            driverId, largePeriodNoised().start.toJavaInstant(), largePeriodNoised().end.toJavaInstant(), random.nextInt(100000, 999999)
        )
    }

    fun genVehicle(): Vehicle {
        val plateNumber = faker.string.regexify("""[А-Я]{1}\d{3}[А-Я]{2}\d{2}""")
        return Vehicle(
            null,
            plateNumber,
            model = faker.vehicle.modelsByMake(""),
            manufactureYear = largePeriod.start.toJavaInstant(),
            length = random.nextDouble(12.0, 15.0),
            width = random.nextDouble(1.5, 2.5),
            height = random.nextDouble(1.5, 4.0),
            loadCapacity = random.nextDouble(1.0, 3.0),
            bodyType = BodyType.values().random()
        )
    }

    fun genOwnerShip(vehicleId: Long, driverId: Long): VehicleOwnership {
        return VehicleOwnership(vehicleId, driverId, largePeriodNoised().start.toJavaInstant(), largePeriodNoised().end.toJavaInstant())
    }

    fun genOrder(customerId: Long, vehicleId: Long): Orders {
        return Orders(
            null,
            customerId = customerId,
            distance = random.nextDouble(30.0, 1000.0),
            price = random.nextDouble(1000.0, 100000.0),
            orderDate = actionsPeriodNoised().start.toJavaInstant(),
            vehicleId = vehicleId,
        )
    }

    fun genCargo( orderId: Long): Cargo {
        return Cargo(
            null,
            weight = random.nextDouble(1.0, 100.0),
            width = random.nextDouble(0.4, 2.5),
            height = random.nextDouble(0.4, 4.0),
            length = random.nextDouble(0.4, 15.0),
            orderId = orderId,
            cargoType = CargoType.values().random(),
        )
    }

    fun genAddress(): Address {
        return Address(
            null,
            country = "Thailand",
            city = faker.address.city(),
            street = faker.address.streetName(),
            building = random.nextInt(1, 100),
            corpus = random.nextInt(1, 10),
        )
    }

    fun genStoragePoint(addressId: Long): StoragePoint {
        return StoragePoint(
            addressId,
            latitude = random.nextDouble(14.0, 17.0),
            longitude = random.nextDouble(98.0, 103.0),
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
            unloadingTime = LocalTime(random.nextInt(1, 12), 0).toJavaLocalTime(),
            loadingTime = LocalTime(random.nextInt(1, 12), 0).toJavaLocalTime(),
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
