package org.ifmo.isbdcurs.controllers

import jakarta.validation.Valid
import org.ifmo.isbdcurs.models.Order
import org.ifmo.isbdcurs.services.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class OrderController @Autowired constructor(private val orderService: OrderService) {
    val logger = org.slf4j.LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping("/index")
    fun showOrdersList(model: Model): String {
        logger.info("showOrdersList")
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
}