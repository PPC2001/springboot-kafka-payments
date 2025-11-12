package com.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    private String transactionId; // Original payment transactionId
    private String userId;
    private double amount;
    private String notificationType;   // EMAIL, SMS, PUSH
}
