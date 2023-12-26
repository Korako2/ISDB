package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.ordering.OrderDetails
import org.ifmo.isbdcurs.customer.ordering.OrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.SessionAttributes

@SessionAttributes("orders")
class CustomerController @Autowired constructor(private val orderService: OrderService) {
    @ModelAttribute("orders")
    fun orders(): List<OrderDetails> {
        return orderService.getOrders()
    }

    @GetMapping("/customer/index")
    fun index(): String {
        return "/customer/index"
    }
}