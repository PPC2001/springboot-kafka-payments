package com.payment.service;

import com.payment.model.NotificationEvent;
import com.payment.model.PaymentResponse;
import com.payment.model.RefundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.Acknowledgment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    // For idempotency
    private final Map<String, String> processedPayments = new ConcurrentHashMap<>();
    private final Map<String, String> processedRefunds = new ConcurrentHashMap<>();

    @KafkaListener(topics = "payment-transactions", groupId = "payment-group")
    public void consumePayments(PaymentResponse payment) {
        if (processedPayments.putIfAbsent(payment.getTransactionId(), "IN_PROGRESS") != null) {
            log.warn("Duplicate payment ignored: {}", payment.getTransactionId());
            return;
        }

        try {
            log.info("Processing payment {} for user {} amount {}", payment.getTransactionId(), payment.getUserId(), payment.getAmount());

            // Example of real-world logic
            if (payment.getAmount() > 10000) {
                log.warn("High-value payment detected: {}", payment.getTransactionId());
                // call anti-fraud service or additional checks
            }

            // Simulate processing delay
            Thread.sleep(500);

            log.info("Payment processed successfully: {}", payment.getTransactionId());
            processedPayments.put(payment.getTransactionId(), "SUCCESS");

        } catch (Exception e) {
            log.error("Payment processing failed for {}: {}", payment.getTransactionId(), e.getMessage(), e);
            throw new RuntimeException(e); // can trigger retry or DLT
        }
    }

    @KafkaListener(topics = "payment-failures", groupId = "failure-group")
    public void consumeFailedPayments(PaymentResponse payment) {
        log.error("Received failed payment for user {} transaction {}", payment.getUserId(), payment.getTransactionId());
        // TODO: trigger alert, retry logic, or manual investigation
    }

    @KafkaListener(topics = "payment-logs", groupId = "log-group")
    public void consumePaymentLogs(PaymentResponse payment) {
        log.info("Audit log for payment {}: {}", payment.getTransactionId(), payment);
    }

    @KafkaListener(topics = "refund-transactions", groupId = "refund-group")
    public void consumeRefunds(RefundEvent refund) {
        if (processedRefunds.putIfAbsent(refund.getRefundId(), "IN_PROGRESS") != null) {
            log.warn("Duplicate refund ignored: {}", refund.getRefundId());
            return;
        }

        try {
            log.info("Processing refund {} for transaction {} user {}", refund.getRefundId(), refund.getTransactionId(), refund.getUserId());
            // Simulate refund logic
            Thread.sleep(300);
            log.info("Refund processed successfully: {}", refund.getRefundId());
            processedRefunds.put(refund.getRefundId(), "SUCCESS");
        } catch (Exception e) {
            log.error("Refund processing failed for {}: {}", refund.getRefundId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void consumeNotifications(NotificationEvent notification) {
        try {
            log.info("Sending {} to user {}", notification.getType(), notification.getUserId());
            switch (notification.getType().toUpperCase()) {
                case "EMAIL" -> sendEmail(notification);
                case "SMS" -> sendSms(notification);
                case "PUSH" -> sendPush(notification);
                default -> log.warn("Unknown notification type: {}", notification.getType());
            }
        } catch (Exception e) {
            log.error("Notification processing failed for {}: {}", notification.getUserId(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void sendEmail(NotificationEvent event) {
        log.info("Email sent to {}: {}", event.getUserId(), event.getMessage());
    }

    private void sendSms(NotificationEvent event) {
        log.info("SMS sent to {}: {}", event.getUserId(), event.getMessage());
    }

    private void sendPush(NotificationEvent event) {
        log.info("Push notification sent to {}: {}", event.getUserId(), event.getMessage());
    }
}
