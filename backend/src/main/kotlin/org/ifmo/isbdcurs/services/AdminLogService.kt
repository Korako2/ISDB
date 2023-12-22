package org.ifmo.isbdcurs.services

import org.ifmo.isbdcurs.models.AdminLogRow
import org.ifmo.isbdcurs.models.LogLevels
import org.ifmo.isbdcurs.persistence.AdminLogRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class AdminLogService(private val adminLogRepository: AdminLogRepository) {
    fun getAdminLog(page: Int, pageSize: Int): List<AdminLogRow> {
        return adminLogRepository.findAll(PageRequest.of(page, pageSize)).toList()
    }

    fun getTotalPages(pageSize: Int): Long {
        return (adminLogRepository.count() + pageSize - 1) / pageSize
    }

    fun addRow(message: String, level: LogLevels) {
        val logEntry = AdminLogRow(message = message, level = level, timestamp = java.util.Date().toInstant())
        adminLogRepository.save(logEntry)
    }
}