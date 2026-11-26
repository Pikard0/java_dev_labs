package com.java.toystore.messaging;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderPlacedEvent(
        UUID eventId,
        Integer orderId,
        String customerUsername,
        BigDecimal totalPrice,
        OffsetDateTime occurredAt
) {
}
