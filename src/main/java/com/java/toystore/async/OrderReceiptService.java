package com.java.toystore.async;

import com.java.toystore.domain.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderReceiptService {

    private final AsyncWorkDelay delay;

    @Async("toyStoreTaskExecutor")
    public CompletableFuture<OrderReceipt> generateReceiptPdf(OrderResponse order) {
        delay.waitForSlowOperation("receipt PDF generation");

        return CompletableFuture.completedFuture(new OrderReceipt(
                order.getId(),
                "order-" + order.getId() + ".pdf",
                Thread.currentThread().getName()
        ));
    }
}
