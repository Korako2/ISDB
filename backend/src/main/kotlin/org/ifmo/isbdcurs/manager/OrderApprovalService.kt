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
        logger.info("Waiting for approval...")

        websocketMessaging.convertAndSend("/topic/manager", "Заказ $orderId ожидает одобрения!")

        logger.info("Approved!")
        approve(orderId)
    }

    fun approve(orderId: Long) {
        logger.info("[approve] called with $orderId")

        websocketMessaging.convertAndSend("/topic/customer", "Ваш заказ $orderId одобрен!")
    }

    fun reject(orderId: Long) {
        logger.info("[reject] called with $orderId")

        websocketMessaging.convertAndSend("/topic/customer", "Ваш заказ $orderId отклонен!")
    }
}