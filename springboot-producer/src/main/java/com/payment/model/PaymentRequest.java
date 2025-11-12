package com.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentRequest {
    private String userId;
    private double amount;
    private String currency;           // e.g., USD, INR
    private String paymentMethod;      // CREDIT_CARD, UPI, WALLET
    private String merchantId;
    private String notificationType;   // EMAIL, SMS, PUSH
}
