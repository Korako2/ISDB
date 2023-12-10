package org.ifmo.isbdcurs.persistence

import org.ifmo.isbdcurs.models.*
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.query.Procedure
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

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
}

interface CustomerRepository : CrudRepository<Customer, Long> {
    @Query("SELECT add_new_customer(:#{#c.firstName}, :#{#c.lastName}, :#{#c.gender}, :#{#c.dateOfBirth}, :#{#c.middleName}), :#{#c.organization}", nativeQuery = true)
    fun addNewCustomer(@Param("c") addCustomerRequest: AddCustomerRequest): Long
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
    fun findSuitableVehicle(@Param("request") request: AddOrderRequest): Long

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

interface OrderRepository : CrudRepository<Order, Long> {
    @Query("SELECT add_order(:#{#v_customer_id}, :#{#v_distance}, :#{#v_vehicle_id}, :#{#v_weight}, :#{#v_width}, :#{#v_height}, :#{#v_length}, :#{#v_cargo_type})", nativeQuery = true)
    fun addOrder(
        @Param("v_customer_id") customerId: Int,
        @Param("v_distance") distance: Int,
        @Param("v_vehicle_id") vehicleId: Int,
        @Param("v_weight") weight: Int,
        @Param("v_width") width: Int,
        @Param("v_height") height: Int,
        @Param("v_length") length: Int,
        @Param("v_cargo_type") cargoType: String,
    ) : Long
}

interface OrderStatusesRepository : CrudRepository<OrderStatuses, OrderStatusesPK>

interface CargoRepository : CrudRepository<Cargo, Long>

interface AddressRepository : CrudRepository<Address, Long>

interface StoragePointRepository : CrudRepository<StoragePoint, Long>

interface LoadingUnloadingAgreementRepository : CrudRepository<LoadingUnloadingAgreement, LoadingUnloadingAgreementPK> {
    fun findByOrderIdAndDriverId(orderId: Long, driverId: Long): LoadingUnloadingAgreement?
}

interface FuelCardsForDriversRepository : CrudRepository<FuelCardsForDrivers, FuelCardsForDriversPK> {
    fun findByFuelCardNumber(fuelCardNumber: String): FuelCardsForDrivers?
}

interface FuelExpensesRepository : CrudRepository<FuelExpenses, FuelExpensesPK>

