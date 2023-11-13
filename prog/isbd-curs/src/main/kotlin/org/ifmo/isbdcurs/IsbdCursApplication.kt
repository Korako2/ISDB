package org.ifmo.isbdcurs

import kotlinx.datetime.Clock
import org.ifmo.isbdcurs.logic.FillTables
import org.ifmo.isbdcurs.persistence.CSVDataDumper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IsbdCursApplication

fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger(IsbdCursApplication::class.java)
    val applicationContext = runApplication<IsbdCursApplication>(*args)

    val fillTables = applicationContext.getBean(FillTables::class.java)
    // take driverCount, customerCount, vehicleCount from args
    val driverCount = args[0].toInt()
    val customersCount = args[1].toInt()
    val dataDir = args[2]
    val startTime = Clock.System.now()
    val allTables = fillTables.createData(driverCount, customersCount)
    logger.info("Data created at ${Clock.System.now()}. Took ${Clock.System.now() - startTime}")
    CSVDataDumper(dataDir).saveTables(allTables)
    logger.info("Insertion finished at ${Clock.System.now()}. Took ${Clock.System.now() - startTime}")
}