package org.ifmo.isbdcurs.persistence

import org.ifmo.isbdcurs.models.*
import org.springframework.data.repository.CrudRepository

interface PersonRepository : CrudRepository<Person, Long>

interface ContactInfoRepository : CrudRepository<ContactInfo, Long>

interface DriverRepository : CrudRepository<Driver, Long>

interface CustomerRepository : CrudRepository<Customer, Long>

interface DriverStatusHistoryRepository : CrudRepository<DriverStatusHistory, Long>

interface TariffRateRepository : CrudRepository<TariffRate, Long>

interface DriverLicenseRepository : CrudRepository<DriverLicense, Long>

interface VehicleRepository : CrudRepository<Vehicle, Long>

interface VehicleOwnershipRepository : CrudRepository<VehicleOwnership, Long>

interface VehicleMovementHistoryRepository : CrudRepository<VehicleMovementHistory, Long>

interface OrderRepository : CrudRepository<Order, Long>

interface OrderStatusesRepository : CrudRepository<OrderStatuses, Long>

interface CargoRepository : CrudRepository<Cargo, Long>

interface AddressRepository : CrudRepository<Address, Long>

interface StoragePointRepository : CrudRepository<StoragePoint, Long>

interface LoadingUnloadingAgreementRepository : CrudRepository<LoadingUnloadingAgreement, Long>

interface FuelCardsForDriversRepository : CrudRepository<FuelCardsForDrivers, Long>

interface FuelExpensesRepository : CrudRepository<FuelExpenses, Long>

