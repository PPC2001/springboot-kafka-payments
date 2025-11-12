package com.payment.service;

import com.payment.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-transactions";
    private static final String PAYMENT_LOG_TOPIC = "payment-logs";
    private static final String PAYMENT_FAILURE_TOPIC = "payment-failures";
    private static final String REFUND_TOPIC = "refund-transactions";
    private static final String NOTIFICATION_TOPIC = "notification-events";

    @Transactional("kafkaTransactionManager")
    public PaymentResponse sendPayment(PaymentRequest request) {
        var payment = buildPayment(request);

        log.info("[{}] Initiating payment for user {} amount {}", payment.getTraceId(), payment.getUserId(), payment.getAmount());

        executeInTransaction(payment.getUserId(), () -> {
            sendToTopics(payment, PAYMENT_TOPIC, PAYMENT_LOG_TOPIC);
            sendNotification(payment.getUserId(), payment.getTraceId(),
                    "Payment SUCCESS for transaction " + payment.getTransactionId(), request.getNotificationType());
        }, payment, PAYMENT_FAILURE_TOPIC);

        payment.setStatus("SUCCESS");
        payment.setTimestamp(System.currentTimeMillis());
        log.info("[{}] Payment SUCCESS for user {} transactionId {}", payment.getTraceId(), payment.getUserId(), payment.getTransactionId());
        return payment;
    }

    public RefundEvent sendRefund(RefundRequest request) {
        var refund = buildRefund(request);
        log.info("[{}] Initiating refund for transaction {} amount {}", refund.getTraceId(), refund.getTransactionId(), refund.getAmount());
        executeInTransaction(refund.getUserId(), () -> {
            sendMessage(refund, REFUND_TOPIC, refund.getUserId());
            sendNotification(refund.getUserId(), refund.getTraceId(),
                    "Refund SUCCESS for transaction " + refund.getTransactionId(),
                    request.getNotificationType(), refund.getTransactionId());
        }, refund, REFUND_TOPIC);

        refund.setStatus("SUCCESS");
        refund.setTimestamp(System.currentTimeMillis());
        log.info("[{}] Refund SUCCESS for user {} refundId {}", refund.getTraceId(), refund.getUserId(), refund.getRefundId());
        return refund;
    }

    // -------------------- Helpers --------------------

    private PaymentResponse buildPayment(PaymentRequest request) {
        var traceId = UUID.randomUUID().toString();
        return PaymentResponse.builder()
                .transactionId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantId(request.getMerchantId())
                .paymentMethod(request.getPaymentMethod())
                .status("INITIATED")
                .traceId(traceId)
                .build();
    }

    private RefundEvent buildRefund(RefundRequest request) {
        var traceId = UUID.randomUUID().toString();
        return RefundEvent.builder()
                .refundId(UUID.randomUUID().toString())
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .status("INITIATED")
                .traceId(traceId)
                .build();
    }

    private void sendToTopics(Object payload, String mainTopic, String logTopic) {
        var key = extractUserId(payload);
        sendMessage(payload, mainTopic, key);
        sendMessage(payload, logTopic, key);
    }

    private void sendNotification(String userId, String traceId, String message, String type) {
        sendNotification(userId, traceId, message, type, null);
    }

    private void sendNotification(String userId, String traceId, String message, String type, String transactionId) {
        var notification = NotificationEvent.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .transactionId(transactionId)
                .traceId(traceId)
                .timestamp(System.currentTimeMillis())
                .build();
        sendMessage(notification, NOTIFICATION_TOPIC, userId);
        log.info("[{}] NotificationEvent sent to user {} on topic {}", traceId, userId, NOTIFICATION_TOPIC);
    }

    private void executeInTransaction(String key, Runnable action, Object payload, String failureTopic) {
        kafkaTemplate.executeInTransaction(kafka -> {
            try {
                action.run();
            } catch (Exception e) {
                log.error("Kafka transaction failed for key {}: {}", key, e.getMessage(), e);
                sendMessage(payload, failureTopic, key);
                throw e;
            }
            return null;
        });
    }

    private void sendMessage(Object payload, String topic, String key) {
        Message<Object> message = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader("kafka_messageKey", key)
                .build();
        kafkaTemplate.send(message);
    }

    private String extractUserId(Object payload) {
        return switch (payload) {
            case PaymentResponse p -> p.getUserId();
            case RefundEvent r -> r.getUserId();
            default -> throw new IllegalArgumentException("Unknown payload type: " + payload.getClass());
        };
    }
}
