package org.ifmo.isbdcurs.persistence

import org.ifmo.isbdcurs.models.*
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import java.util.*

interface PersonRepository : CrudRepository<Person, Long>

interface ContactInfoRepository : CrudRepository<ContactInfo, Long>

interface DriverRepository : CrudRepository<Driver, Long> {
    @Procedure(name = "addDriverInfo")
    fun addDriverInfo(
        @Param("v_driver_id") driverId: Int,
        @Param("v_daily_rate") dailyRate: Int,
        @Param("v_rate_per_km") ratePerKm: Int,
        @Param("v_issue_date") issueDate: java.util.Date,
        @Param("v_expiration_date") expirationDate: java.util.Date,
        @Param("v_license_number") licenseNumber: Int,
        @Param("v_fuel_card") fuelCard: String,
        @Param("v_fuel_station_name") fuelStationName: String,
    )

    @Query("SELECT add_driver(:#{#v.firstName}, :#{#v.lastName}, :#{#v.middleName}, :#{#v.gender}, :#{#v.dateOfBirth}, :#{#v.passport}, :#{#v.bankCardNumber})", nativeQuery = true)
    fun addDriver(@Param("v") addDriverRequest: AddDriverRequest): Long

    fun getDriverById(driverId: Long): Driver

    @Query("SELECT * FROM vehicle " +
            "JOIN vehicle_ownership vo ON vehicle.id = vo.vehicle_id " +
            "WHERE driver_id = :driverId", nativeQuery = true)
    fun getVehicleByDriverId(driverId: Long): Vehicle

    @Query("""
        SELECT 
        new org.ifmo.isbdcurs.models.DriverResponse(
            d.id, 
            p.firstName, p.lastName, 'FIXMEPHONE', 'TODO: EMAIL', dl.licenseNumber, 
            dl.issueDate, dl.expirationDate, d.bankCardNumber)
        FROM Driver d
            JOIN Person p ON d.personId = p.id
            JOIN DriverLicense dl ON d.id = dl.driverId
            JOIN VehicleOwnership vo ON d.id = vo.driverId
            JOIN Vehicle v ON vo.vehicleId = v.id
        WHERE d.id >= :minDriverId AND d.id <= :maxDriverId
    """)
// TODO: fix email & phone number enum exception
//    JOIN ContactInfo c_mail ON p.id = d.personId AND c_mail.contactType = 'EMAIL'
//    JOIN ContactInfo c_phone ON p.id = d.personId AND c_phone.contactType = 'PHONE_NUMBER'
    fun getExtendedDriversPaged(minDriverId: Int, maxDriverId: Int): List<DriverResponse>
}

interface CustomerRepository : CrudRepository<Customer, Long> {
    @Query("SELECT add_new_customer(:#{#c.firstName}, :#{#c.lastName}, :#{#c.gender}, :#{#c.dateOfBirth}, :#{#c.middleName}), :#{#c.organization}", nativeQuery = true)
    fun addNewCustomer(@Param("c") addCustomerRequest: AddCustomerRequest): Long

    @Query("""
        SELECT 
        new org.ifmo.isbdcurs.models.CustomerResponse(
            c.id, p.firstName, p.lastName, p.dateOfBirth, 'FIXMEPHONE', 'FIXMEMAIL')
        FROM Customer c
            JOIN Person p ON c.personId = p.id
        WHERE c.id >= :minCustomerId AND c.id <= :maxCustomerId
    """)

// TODO: fix email & phone number enum exception
//    JOIN ContactInfo c_phone ON p.id = c.personId AND c_phone.hkj
//    JOIN ContactInfo c_mail ON p.id = c.personId AND c_mail.contactType = 'EMAIL'
    fun getExtendedCustomersPaged(minCustomerId: Int, maxCustomerId: Int): List<CustomerResponse>
}

interface DriverStatusHistoryRepository : CrudRepository<DriverStatusHistory, Long> {
    fun findByDriverIdOrderByDateDesc(driverId: Long): List<DriverStatusHistory>
}

interface TariffRateRepository : CrudRepository<TariffRate, Long>

interface DriverLicenseRepository : CrudRepository<DriverLicense, Long>

interface VehicleRepository : CrudRepository<Vehicle, Long> {
    @Query(value = """
        SELECT closest_vehicle_id 
        FROM find_suitable_vehicle(
          v_length => :#{#request.length},
          v_width => :#{#request.width},
          v_height => :#{#request.height},
          v_cargo_type => :#{#request.cargoType},
          v_weight => :#{#request.weight},
          cargo_latitude => :#{#request.latitude},
          cargo_longitude => :#{#request.longitude}
        ) 
    """, nativeQuery = true)
    fun findSuitableVehicle(@Param("request") request: OrderDataForVehicle): Long

//    @Query("SELECT * FROM get_vehicle_coordinates(:vehicleId)", nativeQuery = true)
//    fun getVehicleCoordinates(vehicleId: Long): Coordinates
}


interface VehicleOwnershipRepository : CrudRepository<VehicleOwnership, VehicleOwnershipPK> {
    fun findByDriverId(driverId: Long): List<VehicleOwnership>
    fun findByVehicleId(vehicleId: Long): VehicleOwnership
}

interface VehicleMovementHistoryRepository : CrudRepository<VehicleMovementHistory, VehicleMovementHistoryPK> {
    fun findByVehicleIdOrderByDateDesc(vehicleId: Long): List<VehicleMovementHistory>
}

interface OrderRepository : JpaRepository<Order, Long> {
    @Query("SELECT add_order(:#{#v_customer_id}, :#{#v_distance}, :#{#v_vehicle_id}, :#{#v_weight}, :#{#v_width}, :#{#v_height}, :#{#v_length}, :#{#v_cargo_type}, :#{#v_date})", nativeQuery = true)
    fun addOrder(
        @Param("v_customer_id") customerId: Int,
        @Param("v_distance") distance: Double,
        @Param("v_vehicle_id") vehicleId: Int,
        @Param("v_weight") weight: Double,
        @Param("v_width") width: Double,
        @Param("v_height") height: Double,
        @Param("v_length") length: Double,
        @Param("v_cargo_type") cargoType: String,
        @Param("v_date") date: java.util.Date,
    ) : Long

    @Query("""
        SELECT 
        new org.ifmo.isbdcurs.models.ExtendedOrder(o.id, customer_p.lastName, driver_p.lastName, l.departurePoint, l.deliveryPoint, s.status)
        FROM Order o
            JOIN Customer c ON o.customerId = c.id
            JOIN LoadingUnloadingAgreement l ON o.id = l.orderId
            JOIN Driver d ON l.driverId = d.id
            JOIN OrderStatuses s ON s.orderId = o.id
            JOIN Person customer_p ON c.personId = customer_p.id
            JOIN Person driver_p ON d.personId = driver_p.id
        WHERE s.dateTime = (SELECT MAX(s2.dateTime) FROM OrderStatuses s2 WHERE s2.orderId = o.id)
            AND o.id >= :minOrderId AND o.id <= :maxOrderId
    """)
    fun getExtendedResults(minOrderId: Int, maxOrderId: Int): List<ExtendedOrder>

    @Query("""
        SELECT 
        new org.ifmo.isbdcurs.models.ExtendedOrder(o.id, customer_p.lastName, driver_p.lastName, l.departurePoint, l.deliveryPoint, s.status)
        FROM Order o
            JOIN Customer c ON o.customerId = c.id
            JOIN LoadingUnloadingAgreement l ON o.id = l.orderId
            JOIN Driver d ON l.driverId = d.id
            JOIN OrderStatuses s ON s.orderId = o.id
            JOIN Person customer_p ON c.personId = customer_p.id
            JOIN Person driver_p ON d.personId = driver_p.id
        WHERE s.dateTime = (SELECT MAX(s2.dateTime) FROM OrderStatuses s2 WHERE s2.orderId = o.id)
        AND c.id = :customerId AND o.id >= :minOrderId AND o.id <= :maxOrderId
    """)
    fun getExtendedResultsByCustomerId(customerId: Long, minOrderId: Int, maxOrderId: Int): List<ExtendedOrder>
}

interface OrderStatusesRepository : CrudRepository<OrderStatuses, OrderStatusesPK>

interface CargoRepository : CrudRepository<Cargo, Long>

interface AddressRepository : CrudRepository<Address, Long> {
    fun findByCountryAndCityAndStreetAndBuilding(country: String, city: String, street: String, building: Int): Optional<Address>
}

interface StoragePointRepository : CrudRepository<StoragePoint, Long>

interface LoadingUnloadingAgreementRepository : CrudRepository<LoadingUnloadingAgreement, LoadingUnloadingAgreementPK> {
    fun findByOrderIdAndDriverId(orderId: Long, driverId: Long): LoadingUnloadingAgreement?
}

interface FuelCardsForDriversRepository : CrudRepository<FuelCardsForDrivers, FuelCardsForDriversPK> {
    fun findByFuelCardNumber(fuelCardNumber: String): FuelCardsForDrivers?
}

interface FuelExpensesRepository : CrudRepository<FuelExpenses, FuelExpensesPK>

interface AdminLogRepository : PagingAndSortingRepository<AdminLogRow, Long> {
    fun findByLevel(level: LogLevels, pageable: Pageable): Page<AdminLogRow>

    fun save(logEntry: AdminLogRow): AdminLogRow
}