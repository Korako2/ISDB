package org.ifmo.isbdcurs.persistence

import kotlinx.datetime.Clock
import org.ifmo.isbdcurs.logic.AllTables
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.repository.CrudRepository

class PersistTables : SaveTablesI {
    private val logger: Logger = LoggerFactory.getLogger(PersistTables::class.java)

    private val repositoriesStore = RepositoriesStore();

    private fun <T> saveTable(
        tableName: String,
        table: List<T>,
        repository: CrudRepository<T, *>,
    ) {
        logger.info("Saving $tableName")
        val startTime = Clock.System.now()
        repository.saveAll(table)
        logger.info("Saved $tableName in ${Clock.System.now() - startTime}")
    }

    override fun saveTables(tables: AllTables) {
        logger.info("=== Saving tables")
        val startTime = Clock.System.now()
        saveTable("persons", tables.persons, repositoriesStore.personRepository)
        saveTable("contactInfos", tables.contactInfos, repositoriesStore.contactInfoRepository)
        saveTable("drivers", tables.drivers, repositoriesStore.driverRepository)
        saveTable("customers", tables.customers, repositoriesStore.customerRepository)
        saveTable("driverStatusHistory", tables.driverStatusHistory, repositoriesStore.driverStatusHistoryRepository)
        saveTable("tariffRates", tables.tariffRates, repositoriesStore.tariffRateRepository)
        saveTable("driverLicenses", tables.driverLicenses, repositoriesStore.driverLicenseRepository)
        saveTable("vehicles", tables.vehicles, repositoriesStore.vehicleRepository)
        saveTable("vehicleOwnerships", tables.vehicleOwnerships, repositoriesStore.vehicleOwnershipRepository)
        saveTable(
            "vehicleMovementHistory", tables.vehicleMovementHistory, repositoriesStore.vehicleMovementHistoryRepository
        )
        saveTable("orders", tables.orders, repositoriesStore.orderRepository)
        saveTable("orderStatuses", tables.orderStatuses, repositoriesStore.orderStatusesRepository)
        saveTable("cargos", tables.cargos, repositoriesStore.cargoRepository)
        saveTable("addresses", tables.addresses, repositoriesStore.addressRepository)
        saveTable("storagePoints", tables.storagePoints, repositoriesStore.storagePointRepository)
        saveTable(
            "loadingUnloadingAgreements",
            tables.loadingUnloadingAgreements,
            repositoriesStore.loadingUnloadingAgreementRepository
        )
        saveTable("fuelCardsForDrivers", tables.fuelCardsForDrivers, repositoriesStore.fuelCardsForDriversRepository)
        saveTable("fuelExpenses", tables.fuelExpenses, repositoriesStore.fuelExpensesRepository)
        logger.info("=== Saved tables in ${Clock.System.now() - startTime}")
    }
}