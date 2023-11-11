package org.ifmo.isbdcurs.persistence

import org.ifmo.isbdcurs.logic.AllTables

interface SaveTablesI {
    fun saveTables(tables: AllTables)
}