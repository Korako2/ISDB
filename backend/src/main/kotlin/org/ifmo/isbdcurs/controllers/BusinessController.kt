package org.ifmo.isbdcurs.controllers

import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.models.AddDriverInfoRequest
import org.ifmo.isbdcurs.models.AddDriverRequest
import org.ifmo.isbdcurs.models.Order
import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.DriverService
import org.ifmo.isbdcurs.services.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class BusinessController @Autowired constructor(
    private val orderService: OrderService,
    private val customerService: CustomerService,
    private val driverService: DriverService,
) {
    val logger = org.slf4j.LoggerFactory.getLogger(BusinessController::class.java)

    @GetMapping("/index")
    fun showOrdersList(model: Model): String {
        model.addAttribute("orders", orderService.getAll())
        return "index"
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = orderService.getById(id)

    @PostMapping("/addorder")
    fun create(@Valid order: Order, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-order"
        }
        orderService.create(order)
        return "redirect:/index"
    }

    @PostMapping("/add_customer")
    fun addCustomer(@Valid @RequestBody addCustomerRequest: AddCustomerRequest, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-customer"
        }
        // TODO: может нам понадобится сохранять Id созданного заказчика?
        // TODO: обработка ошибок и вывод клиенту
        customerService.addCustomer(addCustomerRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_driver")
    fun addDriver(@Valid addDriverRequest: AddDriverRequest, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-driver"
        }
//        driverService.addDriver(addDriverRequest)
        return "redirect:/index"
    }

    @PostMapping("/add_driver_info")
    fun addDriverInfo(@Valid @RequestBody addDriverInfoRequest: AddDriverInfoRequest, result: BindingResult, model: Model): String {
        logger.error(addDriverInfoRequest.toString())
        if (result.hasErrors()) {
            return "add-driver-info"
        }
        driverService.addDriverInfo(addDriverInfoRequest)
        return "redirect:/index"
    }
}