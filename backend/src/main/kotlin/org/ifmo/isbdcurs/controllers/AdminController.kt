package org.ifmo.isbdcurs.controllers

import jakarta.servlet.http.HttpServletRequest
import org.ifmo.isbdcurs.models.OrderDataRequest
import org.ifmo.isbdcurs.models.PhysicalParametersRequest
import org.ifmo.isbdcurs.models.StorageAddressRequest
import org.ifmo.isbdcurs.models.TimeParametersRequest
import org.ifmo.isbdcurs.services.AdminLogService
import org.ifmo.isbdcurs.services.OrderService
import org.ifmo.isbdcurs.util.ErrorHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.util.*

@Controller
class AdminController @Autowired constructor(
    private val adminLogService: AdminLogService,
    private val orderService: OrderService
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

    @GetMapping("/admin")
    fun showAdminPage(request: HttpServletRequest): String {
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin"
        }
        return "admin" // todo
    }
    @GetMapping("/admin/orders")
    fun showOrdersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages() || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/admin/orders"
        }
        model.addAttribute("orders", orderService.getOrdersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        //model.addAttribute("totalPages", orderService.getTotalPages) //todo
        model.addAttribute("orderDataRequest",
            OrderDataRequest(
                departureStoragePoint = StorageAddressRequest(
                    country = "Россия",
                    city = "Москва",
                    street = "Ленина",
                    building = 1,
                ),
                deliveryStoragePoint = StorageAddressRequest(
                    country = "Россия",
                    city = "Москва",
                    street = "Ленина",
                    building = 2,
                ),
                orderParameters = PhysicalParametersRequest(
                    length = 1.0,
                    width = 1.0,
                    height = 1.0,
                    weight = 1.0,
                    cargoType = "Тип груза",
                ),
                time = TimeParametersRequest(
                    // TODO: fix
                    loadingTime = Date(),
                    unloadingTime = Date()
                )
            )
        )
        return "orders"
    }


}