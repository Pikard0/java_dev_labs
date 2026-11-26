package com.java.toystore.async;

import com.java.toystore.domain.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final AsyncWorkDelay delay;

    @Async("toyStoreTaskExecutor")
    public void sendOrderConfirmation(String customerUsername, OrderResponse order) {
        delay.waitForSlowOperation("order confirmation email");

        if (customerUsername == null || customerUsername.isBlank()) {
            throw new IllegalArgumentException("Cannot send an order confirmation without a recipient");
        }

        log.info("Order confirmation for order {} was sent to {} on {}",
                order.getId(), customerUsername, Thread.currentThread().getName());
    }
}
