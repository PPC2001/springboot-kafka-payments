package com.payment.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RefundEvent {

    private String refundId;
    private String transactionId;
    private String userId;
    private double amount;
    private String status;      // INITIATED, SUCCESS, FAILED
    private long timestamp;
    private String traceId;     // For tracing
}