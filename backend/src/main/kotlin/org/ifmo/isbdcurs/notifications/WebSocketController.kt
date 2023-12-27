package org.ifmo.isbdcurs.notifications


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

@Controller
class WebSocketController @Autowired constructor(private val template: SimpMessagingTemplate) {
    private val logger: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(WebSocketController::class.java)

    @MessageMapping("/approveOrder")
    @SendTo("/topic/orderApproval")
    fun approveOrder(orderId: String): String {
        logger.info("Order $orderId is approved!")
        return "Заказ $orderId одобрен!"
    }

    @Scheduled(fixedDelay = 5000)
    fun sendNotification() {
        logger.info("Sending notification")
        template.convertAndSend("/topic/orderApproval", "Заказ одобрен!")
    }
}
