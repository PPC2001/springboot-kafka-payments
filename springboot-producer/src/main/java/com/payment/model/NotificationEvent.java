package com.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private String userId;
    private String type;        // EMAIL, SMS, PUSH
    private String message;
    private String transactionId; // Optional: link to payment/refund
    private String traceId;
    private long timestamp;
}