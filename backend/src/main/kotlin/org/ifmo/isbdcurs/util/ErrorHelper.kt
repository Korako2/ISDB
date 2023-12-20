package org.ifmo.isbdcurs.util

import org.ifmo.isbdcurs.services.AdminLogService
import org.ifmo.isbdcurs.services.BackendException

class ErrorHelper(private val adminLogService: AdminLogService) {
    fun addErrorIfFailed(model: org.springframework.ui.Model, f: () -> Unit) {
        try {
            return f()
        } catch (e: BackendException) {
            adminLogService.addRow(e.message!!, org.ifmo.isbdcurs.models.LogLevels.ERROR)
            model.addAttribute("errorMessage", e.message)
        } catch (e: Exception) {
            adminLogService.addRow(e.message!!, org.ifmo.isbdcurs.models.LogLevels.ERROR)
            model.addAttribute("errorMessage", "Internal server error")
        }
    }
}
