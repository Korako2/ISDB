package org.ifmo.isbdcurs.notifications


import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class WebSocketController {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(WebSocketController::class.java)

    @MessageMapping("/approveOrder")
    @SendTo("/topic/customer")
    fun approveOrder(orderId: String): String {
        logger.info("Order $orderId is approved!")
        return "Заказ $orderId одобрен!"
    }

    @MessageMapping("/requestApproval")
    @SendTo("/topic/manager")
    fun requestApproval(orderId: String): String {
        logger.info("Order $orderId is requested!")
        return "Новый заказ $orderId ожидает одобрения!"
    }
}
