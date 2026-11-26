package com.java.toystore.async;

import com.java.toystore.domain.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DeliveryEstimateService {

    private final AsyncWorkDelay delay;

    @Async("toyStoreTaskExecutor")
    public CompletableFuture<DeliveryEstimate> requestEstimate(OrderResponse order) {
        delay.waitForSlowOperation("delivery estimate request");

        return CompletableFuture.completedFuture(new DeliveryEstimate(
                order.getId(),
                LocalDate.now().plusDays(3),
                Thread.currentThread().getName()
        ));
    }
}
