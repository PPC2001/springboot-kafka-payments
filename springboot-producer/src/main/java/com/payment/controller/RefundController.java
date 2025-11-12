package com.payment.controller;

import com.payment.model.RefundEvent;
import com.payment.model.RefundRequest;
import com.payment.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final KafkaProducerService producer;

    @PostMapping
    public ResponseEntity<RefundEvent> refund(@RequestBody RefundRequest request) {
        return ResponseEntity.ok(producer.sendRefund(request));
    }
}
