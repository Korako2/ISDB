package org.ifmo.isbdcurs.manager

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class OrderApprovalService(
    private val websocketMessaging: SimpMessagingTemplate
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(OrderApprovalService::class.java)

    fun requestApproval(orderId: Long) {
        logger.info("[requestApproval] called with $orderId")

        websocketMessaging.convertAndSend("/topic/customer", "Заказ $orderId оформлен!")
        websocketMessaging.convertAndSend("/topic/manager", "Заказ $orderId ожидает одобрения!")
    }

    fun approve(orderId: Long) {
        logger.info("[approve] called with $orderId")

        websocketMessaging.convertAndSend("/topic/customer", "Ваш заказ $orderId одобрен!")
    }
}