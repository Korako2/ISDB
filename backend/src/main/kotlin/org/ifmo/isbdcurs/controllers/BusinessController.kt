package org.ifmo.isbdcurs.controllers

import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.AddCustomerRequest
import org.ifmo.isbdcurs.models.Order
import org.ifmo.isbdcurs.services.CustomerService
import org.ifmo.isbdcurs.services.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class BusinessController @Autowired constructor(private val orderService: OrderService, private val customerService: CustomerService) {
    val logger = org.slf4j.LoggerFactory.getLogger(BusinessController::class.java)

    @GetMapping("/index")
    fun showOrdersList(model: Model): String {
        model.addAttribute("orders", orderService.getAll())
        return "index"
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) = orderService.getById(id)

    @PostMapping("/addorder")
    fun create(@Valid order: Order, result: BindingResult, model: Model) : String {
        if (result.hasErrors()) {
            return "add-order"
        }
        orderService.create(order)
        return "redirect:/index"
    }

    @PostMapping("/add_customer")
    fun addCustomer(@Valid addCustomerRequest: AddCustomerRequest, result: BindingResult, model: Model) : String {
        if (result.hasErrors()) {
            return "add-customer"
        }
        // TODO: может нам понадобится сохранять Id созданного заказчика?
        // TODO: обработка ошибок и вывод клиенту
        customerService.addCustomer(addCustomerRequest)
        return "redirect:/index"
    }
}