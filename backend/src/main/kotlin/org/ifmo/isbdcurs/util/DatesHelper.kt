package org.ifmo.isbdcurs.util

import java.text.SimpleDateFormat
import java.util.*

fun parseDate(date: String): Date {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.parse(date)
}