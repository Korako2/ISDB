package org.ifmo.isbdcurs.util

import kotlin.math.max
import kotlin.math.min


fun pageToIdRangeReversed(totalPages: Int, pageNumber: Int, pageSize: Int): Pair<Int, Int> {
    val mPage = min(pageNumber, totalPages - 1)
    val startOrderId = totalPages * pageSize - mPage * pageSize - pageSize + 1
    val endOrderId = max(totalPages * pageSize - mPage * pageSize, 1)
    // (11, 20)
    return Pair(startOrderId, endOrderId)
}

fun pageToIdRangeNormal(pageNumber: Int, pageSize: Int): Pair<Int, Int> {
    val startOrderId = pageNumber * pageSize + 1
    val endOrderId = pageNumber * pageSize + pageSize
    return Pair(startOrderId, endOrderId)
}