package com.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String transactionId;
    private String userId;
    private double amount;
    private String currency;
    private String merchantId;
    private String paymentMethod;
    private String status;      // INITIATED, SUCCESS, FAILED
    private long timestamp;
    private String traceId;     // For logging/tracing
}