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

    private val companyNames: List<String> = (1..100).map { faker.company.name() };
    private val firstNames: List<String> = (1..100).map { faker.name.firstName() };
    private val lastNames: List<String> = (1..100).map { faker.name.lastName() };
    private val emails: List<String> = (1..100).map { faker.internet.email() };
    private val vehicleModels: List<String> = (1..100).map { faker.vehicle.modelsByMake("") };
    private val addressCities: List<String> = (1..100).map { faker.address.city() };
    private val addressStreets: List<String> = (1..100).map { faker.address.streetName() };

    private fun randomNumericStr(length: Int): String {
        return (1..length).map { ('0'..'9').random(random) }.joinToString("") { it.toString() }
    }

    private fun largePeriodNoised(): TimePeriod {
        val noise = random.nextLong(0, 365).days * 5;
        return TimePeriod(largePeriod.start.plus(noise), largePeriod.end.minus(noise));
    }

    private fun actionsPeriodNoised(): TimePeriod {
        val noise = random.nextLong(5, 60).days;
        return TimePeriod(actionsPeriod.start.plus(noise), actionsPeriod.end.minus(noise));
    }


    fun genPerson(): Person {
        val gender = if (random.nextBoolean()) Gender.M else Gender.F
        val birthDate = LocalDate(largePeriodNoised().start.toLocalDateTime(TimeZone.UTC).year, 1, 1)
        return Person(
            null,
            firstNames.random(random),
            lastNames.random(random),
            null,
            gender,
            birthDate.toJavaLocalDate()
        )
    }

    fun genContactInfo(personId: Long): ContactInfo {
        return ContactInfo(personId, ContactInfoType.EMAIL, emails.random(random))
    }

    fun genDriver(personId: Long): Driver {
        // use kotlin random to generate passport of length 10
        val passport = randomNumericStr(10)
        val creditCard = randomNumericStr(16)
        return Driver(null, personId, passport, creditCard)
    }

    fun genCustomer(personId: Long): Customer {
        return Customer(null, personId, companyNames.random(random))
    }

    fun genTariffRate(driverId: Long): TariffRate {
        return TariffRate(driverId, random.nextInt(50, 100), random.nextInt(5, 10))
    }

    fun genDriverLicense(driverId: Long): DriverLicense {
        return DriverLicense(
            driverId,
            largePeriodNoised().start.toJavaInstant(),
            largePeriodNoised().end.toJavaInstant(),
            random.nextInt(100000, 999999)
        )
    }

    private fun nRandRusLetters(n: Int): String {
        return (1..n).map { ('А'..'Я').random(random) }.joinToString("")
    }

    private fun nRandRusDigits(n: Int): String {
        return (1..n).map { ('0'..'9').random(random) }.joinToString("")
    }

    fun genVehicle(): Vehicle {
        val plateNumber = nRandRusLetters(1) + nRandRusDigits(3) + nRandRusLetters(2) + nRandRusDigits(2);
        return Vehicle(
            null,
            plateNumber,
            model = vehicleModels.random(random),
            manufactureYear = largePeriod.start.toJavaInstant(),
            length = random.nextDouble(12.0, 15.0),
            width = random.nextDouble(2.0, 2.5),
            height = random.nextDouble(3.0, 4.0),
            loadCapacity = random.nextDouble(1000.0, 3000.0),
            bodyType = BodyType.values().random(random)
        )
    }

    fun genOwnerShip(vehicleId: Long, driverId: Long): VehicleOwnership {
        return VehicleOwnership(vehicleId, driverId, largePeriodNoised().start.toJavaInstant(), null)
    }

    fun genOrder(customerId: Long, vehicleId: Long): Orders {
        return Orders(
            null,
            customerId = customerId,
            distance = random.nextDouble(30.0, 1000.0).toFloat(),
            price = random.nextDouble(1000.0, 100000.0),
            orderDate = actionsPeriodNoised().start.toJavaInstant(),
            vehicleId = vehicleId,
        )
    }

    fun genCargo(orderId: Long): Cargo {
        return Cargo(
            null,
            weight = random.nextDouble(2.0, 100.0).toFloat(),
            width = random.nextDouble(0.4, 2.0).toFloat(),
            height = random.nextDouble(0.4, 3.0).toFloat(),
            length = random.nextDouble(0.4, 10.0).toFloat(),
            orderId = orderId,
            cargoType = CargoType.values().random(random),
        )
    }

    fun genAddress(): Address {
        return Address(
            null,
            country = "Thailand",
            city = addressCities.random(random),
            street = addressStreets.random(random),
            building = random.nextInt(1, 100),
            corpus = random.nextInt(1, 10),
        )
    }

    fun genStoragePoint(addressId: Long): StoragePoint {
        return StoragePoint(
            addressId,
            latitude = random.nextDouble(14.0, 17.0).toFloat(),
            longitude = random.nextDouble(98.0, 103.0).toFloat(),
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
