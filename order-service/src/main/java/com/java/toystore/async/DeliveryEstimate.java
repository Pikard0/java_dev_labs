package com.java.toystore.async;

import java.time.LocalDate;

public record DeliveryEstimate(Integer orderId, LocalDate deliveryDate, String calculatedByThread) {
}
