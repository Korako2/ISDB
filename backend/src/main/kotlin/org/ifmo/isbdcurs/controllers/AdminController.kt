package org.ifmo.isbdcurs.controllers

import org.ifmo.isbdcurs.services.AdminLogService
import org.ifmo.isbdcurs.util.ErrorHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AdminController @Autowired constructor(
    private val adminLogService: AdminLogService,
) {
    private val errorHelper = ErrorHelper(adminLogService)

    @GetMapping("/logs")
    fun showLogs(model: Model): String {
        errorHelper.addErrorIfFailed(model) {
            model.addAttribute("logs", adminLogService.getAdminLog(0, 100))
        }
        // TODO: create logs table
        return "logs"
    }
}