package org.ifmo.isbdcurs.customer

import org.ifmo.isbdcurs.customer.ordering.CustomerOrderService
import org.ifmo.isbdcurs.customer.ordering.OrderDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView

@Controller
@SessionAttributes("selectedOrder", "name")
class OrderDetailsController @Autowired constructor(private val orderService: CustomerOrderService) {
    @ModelAttribute("selectedOrder")
    fun orders(@RequestParam("selectedOrderId") orderId: Long): OrderDetails {
        return orderService.getOrder(orderId)
    }

    @PostMapping("/customer/showOrderDetails")
    fun showOrderDetails(@RequestParam("selectedOrderId") orderId: Long, model: Model): RedirectView {
        return RedirectView("/customer/showOrderDetails?selectedOrderId=$orderId")
    }

    @GetMapping("/customer/showOrderDetails")
    fun showOrderDetails(model: Model): String {
        return "/customer/active_order_details"
    }
}