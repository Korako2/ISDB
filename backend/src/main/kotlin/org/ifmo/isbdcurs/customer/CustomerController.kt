package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.ordering.CustomerOrderService
import org.ifmo.isbdcurs.customer.ordering.OrderDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.SessionAttributes

@Controller
@SessionAttributes("orders", "name")
class CustomerController @Autowired constructor(private val orderService: CustomerOrderService) {
    @ModelAttribute("orders")
    fun orders(): List<OrderDetails> {
        return orderService.getOrders()
    }

    @ModelAttribute("name")
    fun name(@AuthenticationPrincipal userDetails: UserDetails): String {
        return userDetails.username
    }

    @GetMapping("/customer/index")
    fun index(model: Model): String {
        return "/customer/index"
    }
}