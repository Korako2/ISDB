package org.ifmo.isbdcurs.persistence

import org.ifmo.isbdcurs.models.*
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface PersonRepository : CrudRepository<Person, Long>

interface ContactInfoRepository : CrudRepository<ContactInfo, Long>

interface DriverRepository : CrudRepository<Driver, Long>

interface CustomerRepository : CrudRepository<Customer, Long>

interface DriverStatusHistoryRepository : CrudRepository<DriverStatusHistory, Long>

interface TariffRateRepository : CrudRepository<TariffRate, Long>

interface DriverLicenseRepository : CrudRepository<DriverLicense, Long>

interface VehicleRepository : CrudRepository<Vehicle, Long>

interface VehicleOwnershipRepository : CrudRepository<VehicleOwnership, VehicleOwnershipPK>

interface VehicleMovementHistoryRepository : CrudRepository<VehicleMovementHistory, VehicleMovementHistoryPK> {
    @Query("""
        INSERT INTO vehicle_movement_history (vehicle_id, date, latitude, longitude, mileage)
        VALUES (:#{#mh.vehicleId }, :#{#mh.date }, :#{#mh.latitude }, :#{#mh.longitude }, :#{#mh.mileage })
    """)
//        ON CONFLICT (vehicle_id, date)
//        DO UPDATE SET latitude = :#{#mh.latitude }, longitude = :#{#mh.longitude }, mileage = :#{#mh.mileage }
    fun save(@Param("mh") vehicleMovementHistory: VehicleMovementHistory): VehicleMovementHistory;
}

interface OrderRepository : CrudRepository<Orders, Long>

interface OrderStatusesRepository : CrudRepository<OrderStatuses, OrderStatusesPK>

interface CargoRepository : CrudRepository<Cargo, Long>

interface AddressRepository : CrudRepository<Address, Long>

interface StoragePointRepository : CrudRepository<StoragePoint, Long>

interface LoadingUnloadingAgreementRepository : CrudRepository<LoadingUnloadingAgreement, LoadingUnloadingAgreementPK>

interface FuelCardsForDriversRepository : CrudRepository<FuelCardsForDrivers, FuelCardsForDriversPK>

interface FuelExpensesRepository : CrudRepository<FuelExpenses, FuelExpensesPK>

