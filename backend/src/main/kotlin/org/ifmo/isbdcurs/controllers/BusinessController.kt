package org.ifmo.isbdcurs.controllers

import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.*
import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.DriverService
import org.ifmo.isbdcurs.services.OrderService
import org.ifmo.isbdcurs.services.StoragePointService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    val logger = org.slf4j.LoggerFactory.getLogger(BusinessController::class.java)

    @GetMapping("/index")
    fun showOrdersList(model: Model): String {
        model.addAttribute("orders", orderService.getAll())
        return "index"
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = orderService.getById(id)

//    @GetMapping("/orders")
//    fun showOrdersListPage(model: Model, @RequestParam pageNumber: Int, @RequestParam pageSize: Int): String {
//        model.addAttribute("orders", orderService.getOrdersPage(pageNumber, pageSize))
//        return "index"
//    }

    // @GetMapping("/orders")
//    fun showCustomerOrders(model: Model, @RequestParam pageNumber: Int, @RequestParam pageSize: Int , ): String {
//        // TODO: get customer Id from session
//        val customerId = -1L;
//        model.addAttribute("orders", orderService.getOrdersByCustomerId(customerId, pageNumber, pageSize))
//        return "index"
//    }

    @PostMapping("/add_order")
    fun addOrder(@Valid @RequestBody addOrderRequest: AddOrderRequest, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-order"
        }
        orderService.addOrder(addOrderRequest)
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
    fun addDriver(@Valid @RequestBody addDriverRequest: AddDriverRequest, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-driver"
        }
        driverService.addDriver(addDriverRequest)
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

    @PostMapping("/add_storagepoint")
    fun addAddress(@Valid @RequestBody addAddressRequest: AddStoragePointRequest, result: BindingResult, model: Model): String {
        if (result.hasErrors()) {
            return "add-address"
        }
        storagePointService.addStoragePoint(addAddressRequest)
        return "redirect:/index"
    }
}