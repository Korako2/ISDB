package org.ifmo.isbdcurs

import kotlinx.datetime.Clock
import org.ifmo.isbdcurs.generator.CSVDataDumper
import org.ifmo.isbdcurs.generator.FillTables
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext

@SpringBootApplication
class IsbdCursApplication

fun fillTables(applicationContext: ConfigurableApplicationContext, args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger(IsbdCursApplication::class.java)

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

fun main(args: Array<String>) {
    runApplication<IsbdCursApplication>(*args)
}