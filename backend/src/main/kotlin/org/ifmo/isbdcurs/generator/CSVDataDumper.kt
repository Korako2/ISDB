package org.ifmo.isbdcurs.generator

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.concurrent.thread
import kotlin.reflect.full.*


class CSVDataDumper(
    private val dir: String,
) : SaveTablesI {

    private inline fun <reified T : Any> convertTableToCsv(
        tableName: String,
        table: List<T>,
    ) {
        // create dir if not exists
        if (!java.io.File(dir).exists()) {
            java.io.File(dir).mkdirs()
        }
        val writer = BufferedWriter(FileWriter("$dir/$tableName.csv"))

        val csvFormat: CSVFormat = CSVFormat.DEFAULT.builder().build()

        val csvPrinter = CSVPrinter(writer, csvFormat)

//        csvPrinter.printRecord(table[0]!!::class.java.declaredFields.map { it.name.camelToSnakeCase() })
        table.forEach { t ->
            // kotlin get all properties of data class values
            val constructorParamNames = t::class.primaryConstructor?.parameters?.map { p -> p.name }
            val sortedProperties = t::class.declaredMemberProperties.sortedBy { p ->
                constructorParamNames?.indexOf(p.name)
            }
            csvPrinter.printRecord(sortedProperties.map { p ->
                val value = p.getter.call(t) ?: return@map null
                value.toString().split(".").let {
                    if (it.size > 1) {
                        it[0] + "." + it[1].take(3)
                    } else {
                        it[0]
                    }
                }
            })
        }

        csvPrinter.flush()
        csvPrinter.close()
    }

    private inline fun <reified T : Any> saveTable(
        tableName: String,
        table: List<T>,
    ) {
        println("Saving $tableName")
        convertTableToCsv(tableName, table)
    }

    override fun saveTables(tables: AllTables) {
        println("=== Saving tables")
        val t1 = thread {
            saveTable("persons", tables.persons)
            saveTable("contactInfos", tables.contactInfos)
            saveTable("drivers", tables.drivers)
            saveTable("customers", tables.customers)
            saveTable("orders", tables.orders)
            saveTable("orderStatuses", tables.orderStatuses)
        }
        val t2 = thread {
            saveTable("driverStatusHistory", tables.driverStatusHistory)
            saveTable("tariffRates", tables.tariffRates)
            saveTable("driverLicenses", tables.driverLicenses)
            saveTable("vehicles", tables.vehicles)
        }
        val t3 = thread {
            saveTable("vehicleOwnerships", tables.vehicleOwnerships)
            saveTable("vehicleMovementHistory", tables.vehicleMovementHistory)
        }
        val t4 = thread {
            saveTable("cargos", tables.cargos)
            saveTable("storagePoints", tables.storagePoints)
            saveTable("loadingUnloadingAgreements", tables.loadingUnloadingAgreements)
            saveTable("fuelCardsForDrivers", tables.fuelCardsForDrivers)
            saveTable("fuelExpenses", tables.fuelExpenses)
            saveTable("addresses", tables.addresses)
        }
        t1.join()
        t2.join()
        t3.join()
        t4.join()
    }
}