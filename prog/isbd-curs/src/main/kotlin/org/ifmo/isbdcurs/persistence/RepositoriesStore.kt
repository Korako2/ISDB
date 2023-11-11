package org.ifmo.isbdcurs.persistence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RepositoriesStore {
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
}