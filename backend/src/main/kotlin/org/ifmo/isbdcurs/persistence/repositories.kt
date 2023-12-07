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
}

interface CustomerRepository : CrudRepository<Customer, Long> {
    @Query("SELECT add_new_customer(:#{#c.firstName}, :#{#c.lastName}, :#{#c.gender}, :#{#c.dateOfBirth}, :#{#c.middleName}), :#{#c.organization}", nativeQuery = true)
    fun addNewCustomer(@Param("c") addCustomerRequest: AddCustomerRequest): Long
}

interface DriverStatusHistoryRepository : CrudRepository<DriverStatusHistory, Long>

interface TariffRateRepository : CrudRepository<TariffRate, Long>

interface DriverLicenseRepository : CrudRepository<DriverLicense, Long>

interface VehicleRepository : CrudRepository<Vehicle, Long>

interface VehicleOwnershipRepository : CrudRepository<VehicleOwnership, VehicleOwnershipPK> {
    fun findByDriverId(driverId: Long): List<VehicleOwnership>
}

interface VehicleMovementHistoryRepository : CrudRepository<VehicleMovementHistory, VehicleMovementHistoryPK> {
    fun findByVehicleId(vehicleId: Long): List<VehicleMovementHistory>
}

interface OrderRepository : CrudRepository<Order, Long> {}

interface OrderStatusesRepository : CrudRepository<OrderStatuses, OrderStatusesPK>

interface CargoRepository : CrudRepository<Cargo, Long>

interface AddressRepository : CrudRepository<Address, Long>

interface StoragePointRepository : CrudRepository<StoragePoint, Long>

interface LoadingUnloadingAgreementRepository : CrudRepository<LoadingUnloadingAgreement, LoadingUnloadingAgreementPK>

interface FuelCardsForDriversRepository : CrudRepository<FuelCardsForDrivers, FuelCardsForDriversPK> {
    fun findByFuelCardNumber(fuelCardNumber: String): FuelCardsForDrivers?
}

interface FuelExpensesRepository : CrudRepository<FuelExpenses, FuelExpensesPK>

