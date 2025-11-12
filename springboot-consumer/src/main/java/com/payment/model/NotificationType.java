package com.payment.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    EMAIL("EMAIL"),
    SMS("SMS"),
    PUSH("PUSH");

    private final String value;
}
