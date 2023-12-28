package org.ifmo.isbdcurs.controllers

import org.ifmo.isbdcurs.manager.OrderApprovalService
import org.ifmo.isbdcurs.persistence.VehicleOwnershipRepository
import org.ifmo.isbdcurs.services.BackendException
import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.DriverService
import org.ifmo.isbdcurs.services.OrderService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@SessionAttributes("name")
class AdminController(
    private val orderService: OrderService,
    private val driverService: DriverService,
    private val customerService: CustomerService,
    private val approvalService: OrderApprovalService,
    private val vehicleOwnershipRepository: VehicleOwnershipRepository,
    ) {
    private val logger = org.slf4j.LoggerFactory.getLogger(AdminController::class.java)


    @ModelAttribute("name")
    fun name(@AuthenticationPrincipal userDetails: UserDetails): String {
        return userDetails.username
    }
    @GetMapping("/manager")
    fun showLogs(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                 @RequestParam(defaultValue = "10") pageSize: Int,
                 redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPagesForManager(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/manager"
        }
        model.addAttribute("orders", orderService.getOrdersForManager(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", orderService.getTotalPagesForManager(pageSize).toInt())
        model.addAttribute("ords", orderService.getFullOrdersInfo(pageNumber, pageSize))
        return "manager"
    }

    @GetMapping("/manager/orders")
    fun showOrdersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                           @RequestParam(defaultValue = "10") pageSize: Int,
                           redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > orderService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/manager/orders"
        }
        model.addAttribute("orders", orderService.getOrdersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", orderService.getTotalPages(pageSize))
        return "tables/orders"
    }

    @GetMapping("/manager/drivers")
    fun showDriversListPage(model: ModelMap, @RequestParam(defaultValue = "0") pageNumber: Int,
                            @RequestParam(defaultValue = "10") pageSize: Int,
                            redirectAttributes: RedirectAttributes
    ): ModelAndView {
        if (pageNumber < 0 || pageNumber > driverService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            logger.info("Redirecting to /manager/drivers")
            return ModelAndView("redirect:/manager/drivers", model)
        }
        model.addAttribute("drivers", driverService.getDriversPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", driverService.getTotalPages(pageSize))
        return ModelAndView("tables/drivers", model)
    }

    @GetMapping("/manager/customers")
    fun showCustomersListPage(model: Model, @RequestParam(defaultValue = "0") pageNumber: Int,
                            @RequestParam(defaultValue = "10") pageSize: Int,
                            redirectAttributes: RedirectAttributes
    ): String {
        if (pageNumber < 0 || pageNumber > customerService.getTotalPages(pageSize) || pageSize != 10) {
            redirectAttributes.addAttribute("pageNumber", 0)
            redirectAttributes.addAttribute("pageSize", 10)
            return "redirect:/manager/customers"
        }
        model.addAttribute("customers", customerService.getCustomersPaged(pageNumber, pageSize))
        model.addAttribute("currentPage", pageNumber)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", 5)
        model.addAttribute("totalPages", customerService.getTotalPages(pageSize))
        return "tables/customers"
    }

    @GetMapping("/manager/find_suitable_driver")
    fun showFindSuitableDriverPage(model: Model, @RequestParam orderId: Long): String {
        model.addAttribute("orderById", orderService.getFullOrderInfoById(orderId))
        return "find_suitable_driver"
    }

    @GetMapping("/manager/suitable_driver")
    fun showFindSuitableDriver(model: Model, @RequestParam orderId: Long): String {
        model.addAttribute("orderById", orderService.getFullOrderInfoById(orderId))
        try {
            val driverId = orderService.findSuitableDriver(orderId)
            model.addAttribute("driver", driverService.getSuitableDriverResponseByDriverId(driverId))
            return "suitable_driver"
        } catch (e: BackendException) {
            model.addAttribute("error", e.message)
            return "find_suitable_driver"
        }
    }

    @GetMapping("/manager/approve_driver")
    fun approveSuitableDriver(model: Model, @RequestParam orderId: Long, @RequestParam driverId: Long): String {
        logger.info("Approving driver $driverId for order $orderId")
        val vehicleId = vehicleOwnershipRepository.findByDriverId(driverId).firstOrNull()?.vehicleId ?: throw BackendException("No vehicle for driver $driverId")
        orderService.updateOrderWhenVehicleFound(orderId, vehicleId = vehicleId, driverId = driverId)
        approvalService.approve(orderId)
        orderService.startDriverWorker(driverId = driverId, orderId = orderId)
        return "redirect:/manager"
    }

    @GetMapping("/manager/reject_order")
    fun rejectOrder(model: Model, @RequestParam orderId: Long): String {
        orderService.rejectOrder(orderId)
        approvalService.reject(orderId)
        return "redirect:/manager"
    }

}