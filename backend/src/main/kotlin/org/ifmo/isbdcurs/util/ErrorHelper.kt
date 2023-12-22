package org.ifmo.isbdcurs.util

import org.ifmo.isbdcurs.services.AdminLogService
import org.ifmo.isbdcurs.services.BackendException

class ErrorHelper(private val adminLogService: AdminLogService) {
    val logger = org.slf4j.LoggerFactory.getLogger(ErrorHelper::class.java)
    fun addErrorIfFailed(model: org.springframework.ui.Model, f: () -> Unit) {
        try {
            return f()
        } catch (e: BackendException) {
            adminLogService.addRow(e.message!!, org.ifmo.isbdcurs.models.LogLevels.ERROR)
            model.addAttribute("errorMessage", e.message)
        } catch (e: Exception) {
            adminLogService.addRow(e.message!!, org.ifmo.isbdcurs.models.LogLevels.ERROR)
            logger.error(e.message, e)
            model.addAttribute("errorMessage", "Internal server error. Check your input data")
        }
    }

    fun addErrorIfFailed(model: org.springframework.ui.ModelMap, f: () -> Unit) {
        try {
            return f()
        } catch (e: BackendException) {
            adminLogService.addRow(e.message!!, org.ifmo.isbdcurs.models.LogLevels.ERROR)
            logger.error(e.message, e)
            model.addAttribute("errorMessage", e.message)
        } catch (e: Exception) {
            adminLogService.addRow(e.message!!, org.ifmo.isbdcurs.models.LogLevels.ERROR)
            logger.error(e.message, e)
            model.addAttribute("errorMessage", "Internal server error")
        }
    }
}
