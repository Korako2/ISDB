package org.ifmo.isbdcurs.util

import org.ifmo.isbdcurs.models.DriverStatus
import org.ifmo.isbdcurs.models.OrderStatus
import java.util.*

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

// String extensions
fun String.camelToSnakeCase(): String {
    return camelRegex.replace(this) {
        "_${it.value}"
    }.lowercase(Locale.getDefault())
}
