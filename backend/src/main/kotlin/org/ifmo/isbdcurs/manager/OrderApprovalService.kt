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

        // TODO: replace with real approval
        Thread.sleep(5000)

        logger.info("Approved!")
        approve(orderId)
    }

    fun approve(orderId: Long) {
        logger.info("[approve] called with $orderId")

        websocketMessaging.convertAndSend("/topic/orderApproval", "Ваш заказ $orderId одобрен!")
    }
}