package org.ifmo.isbdcurs

import org.ifmo.isbdcurs.logic.FillTables
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class IsbdCursApplication

fun main(args: Array<String>) {
    val applicationContext = runApplication<IsbdCursApplication>(*args)

    val fillTables = applicationContext.getBean(FillTables::class.java)
    fillTables.fill();
}