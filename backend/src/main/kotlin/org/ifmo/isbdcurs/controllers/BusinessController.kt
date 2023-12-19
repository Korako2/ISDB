package org.ifmo.isbdcurs.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.services.*
import org.ifmo.isbdcurs.util.ExceptionHelper
import org.ifmo.isbdcurs.util.addErrorIfFailed
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Controller
class BusinessController @Autowired constructor(
    private val orderService: OrderService,
    private val customerService: CustomerService,
    private val driverService: DriverService,
    private val storagePointService: StoragePointService,
) {
    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(BusinessController::class.java)

    @GetMapping("/index")
    fun showOrdersList(request: HttpServletRequest): String {
        // redirects to appropriate page depending on user role
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin"
        }
        return "redirect:/orders"
    }

    @GetMapping("/orders")
    fun showOrdersListPage(model: Model, @RequestParam pageNumber: Int, @RequestParam pageSize: Int): String {
        addErrorIfFailed(model) {
            val ordersPaged = orderService.getOrdersPaged(pageNumber, pageSize)
            model.addAttribute("orders", ordersPaged)
        }
        return "index"
    }

    // @GetMapping("/orders")
    fun showCustomerOrders(model: Model, @RequestParam pageNumber: Int, @RequestParam pageSize: Int): String {
        // TODO: get customer Id from session
        val customerId = -1L

        addErrorIfFailed(model) {
            val ordersPaged = orderService.getOrdersByCustomerId(customerId, pageNumber, pageSize)
            model.addAttribute("orders", ordersPaged)
        }
        return "index"
    }

    @PostMapping("/add_order")
    fun addOrder(@Valid @RequestBody addOrderRequest: AddOrderRequest, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-order"
        }
        addErrorIfFailed(model) {
            orderService.addOrder(addOrderRequest)
        }
        return "redirect:/index"
    }

    @PostMapping("/add_customer")
    fun addCustomer(@Valid @RequestBody addCustomerRequest: AddCustomerRequest, result: BindingResult, model: Model): String {
        // TODO: может нам понадобится сохранять Id созданного заказчика?
        // TODO: обработка ошибок и вывод клиенту

        customerService.addCustomer(addCustomerRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_driver")
    fun addDriver(@Valid @RequestBody addDriverRequest: AddDriverRequest, result: BindingResult, model: Model): String {
        driverService.addDriver(addDriverRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_driver_info")
    fun addDriverInfo(@Valid @RequestBody addDriverInfoRequest: AddDriverInfoRequest, result: BindingResult, model: Model): String {
        driverService.addDriverInfo(addDriverInfoRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_storagepoint")
    fun addAddress(@Valid @RequestBody addAddressRequest: AddStoragePointRequest, result: BindingResult, model: Model): String {
        storagePointService.addStoragePoint(addAddressRequest)
        return "redirect:/index"
    }
}