package org.ifmo.isbdcurs.controllers

import org.ifmo.isbdcurs.services.AdminLogService
import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.DriverService
import org.ifmo.isbdcurs.services.OrderService
import org.ifmo.isbdcurs.util.ErrorHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AdminController @Autowired constructor(
    private val adminLogService: AdminLogService,
    private val orderService: OrderService,
    private val driverService: DriverService,
    private val customerService: CustomerService
    ) {
    private val errorHelper = ErrorHelper(adminLogService)

    @GetMapping("/admin")
    fun showLogs(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                 @RequestParam(defaultValue = "10") pageSize: Int,
                 redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin"
        }
        model.addAttribute("logs", adminLogService.getAdminLog(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 2)
        // model.addAttribute("totalPages", adminLogService.getTotalPages(pageSize)) // todo add getTotalPages to AdminLogService

//        errorHelper.addErrorIfFailed(model) { // todo I don't understand what I need to do with this.
//
//        }
        return "admin"
    }

    @GetMapping("/admin/orders")
    fun showOrdersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin/orders"
        }
        model.addAttribute("orders", orderService.getOrdersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", orderService.getTotalPages(pageSize))
        return "tables/orders"
    }

    @GetMapping("/admin/drivers")
    fun showDriversListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > driverService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin/drivers"
        }
        model.addAttribute("drivers", driverService.getDriversPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", driverService.getTotalPages(pageSize))
        return "tables/drivers"
    }

    @GetMapping("/admin/customers")
    fun showCustomersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                            @RequestParam(defaultValue = "10") pageSize: Int,
                            redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > customerService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin/customers"
        }
        model.addAttribute("customers", customerService.getCustomersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", customerService.getTotalPages(pageSize))
        return "tables/customers"
    }

}