package com.java.toystore.async;

import java.time.LocalDate;

public record OrderFulfillmentPreview(
        Integer orderId,
        String receiptFileName,
        LocalDate estimatedDeliveryDate,
        boolean available,
        String message
) {

    public static OrderFulfillmentPreview unavailable(Integer orderId, String message) {
        return new OrderFulfillmentPreview(orderId, null, null, false, message);
    }
}
