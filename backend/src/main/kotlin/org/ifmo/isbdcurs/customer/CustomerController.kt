package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.data.CustomerOrderDto
import org.ifmo.isbdcurs.customer.ordering.CustomerOrderService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.SessionAttributes

@Controller
@SessionAttributes("orders", "name")
class CustomerController @Autowired constructor(private val orderService: CustomerOrderService) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CustomerController::class.java)
    @ModelAttribute
    fun orders(model: Model) {
        model.addAttribute("orders", orderService.getActiveOrders())
    }

    @ModelAttribute("name")
    fun name(@AuthenticationPrincipal userDetails: UserDetails): String {
        return userDetails.username
    }

    @GetMapping("/customer/index")
    fun index(model: Model): String {
        return "/customer/index"
    }

    @GetMapping("/customer/completedOrders")
    fun showCompletedOrders(@RequestParam(defaultValue = "0") page: Long, @RequestParam(defaultValue = "10") pageSize: Long, model: Model): String {
        logger.debug("[showCompletedOrders] called with page=$page, pageSize=$pageSize")
        val completedOrdersPage = orderService.getCompletedOrders(page, pageSize)
        model.addAttribute("currentPage", page)
        model.addAttribute("pageSize", pageSize)
        model.addAttribute("totalPages", completedOrdersPage.totalPages)
        model.addAttribute("completedOrders", completedOrdersPage.content)
        return "/customer/completed_orders"
    }
}