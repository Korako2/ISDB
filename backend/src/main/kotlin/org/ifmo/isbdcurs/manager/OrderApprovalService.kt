package org.ifmo.isbdcurs.manager

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.lang.Thread.sleep
import kotlin.concurrent.thread

@Service
class OrderApprovalService(
    private val websocketMessaging: SimpMessagingTemplate
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(OrderApprovalService::class.java)

    fun requestApproval(orderId: Long) {
        logger.info("[requestApproval] called with $orderId")

        thread {
            sleep(3500)
            logger.info("[requestApproval] sending message to /topic/customer")
            websocketMessaging.convertAndSend("/topic/customer", "Заказ $orderId оформлен!")
            websocketMessaging.convertAndSend("/topic/manager", "Заказ $orderId ожидает одобрения!")
        }
    }

    fun approve(orderId: Long) {
        logger.info("[approve] called with $orderId")

        thread {
            sleep(3500)
            websocketMessaging.convertAndSend("/topic/customer", "Ваш заказ $orderId одобрен!")
        }
    }

    fun reject(orderId: Long) {
        logger.info("[reject] called with $orderId")

        websocketMessaging.convertAndSend("/topic/customer", "Ваш заказ $orderId отклонен!")
    }
}