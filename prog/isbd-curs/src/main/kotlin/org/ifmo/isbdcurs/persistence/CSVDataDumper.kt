package org.ifmo.isbdcurs.persistence

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.ifmo.isbdcurs.logic.AllTables
import org.ifmo.isbdcurs.util.camelToSnakeCase
import java.io.BufferedWriter
import java.io.FileWriter
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaField


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
                val value = p.getter.call(t);
                val isEnum = (p.returnType.classifier as KClass<*>).java.isEnum
                if (isEnum || value?.toString()?.any { it.isDigit() } == true) {
                    value?.toString()
                } else {
                    value?.toString()?.camelToSnakeCase();
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
        saveTable("persons", tables.persons)
        saveTable("contactInfos", tables.contactInfos)
        saveTable("drivers", tables.drivers)
        saveTable("customers", tables.customers)
        saveTable("driverStatusHistory", tables.driverStatusHistory)
        saveTable("tariffRates", tables.tariffRates)
        saveTable("driverLicenses", tables.driverLicenses)
        saveTable("vehicles", tables.vehicles)
        saveTable("vehicleOwnerships", tables.vehicleOwnerships)
        saveTable("vehicleMovementHistory", tables.vehicleMovementHistory)
        saveTable("orders", tables.orders)
        saveTable("orderStatuses", tables.orderStatuses)
        saveTable("cargos", tables.cargos)
        saveTable("addresses", tables.addresses)
        saveTable("storagePoints", tables.storagePoints)
        saveTable("loadingUnloadingAgreements", tables.loadingUnloadingAgreements)
        saveTable("fuelCardsForDrivers", tables.fuelCardsForDrivers)
        saveTable("fuelExpenses", tables.fuelExpenses)
    }
}