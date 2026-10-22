package com.java.toystore.async;

import com.java.toystore.configuration.AsyncTaskProperties;
import com.java.toystore.domain.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFulfillmentService {

    private final OrderReceiptService orderReceiptService;
    private final DeliveryEstimateService deliveryEstimateService;
    private final AsyncTaskProperties properties;

    public CompletableFuture<OrderFulfillmentPreview> buildPreview(OrderResponse order) {
        try {
            CompletableFuture<OrderReceipt> receiptFuture = orderReceiptService.generateReceiptPdf(order);
            CompletableFuture<DeliveryEstimate> deliveryFuture = deliveryEstimateService.requestEstimate(order);

            return receiptFuture
                    .thenCombine(deliveryFuture, (receipt, delivery) -> new OrderFulfillmentPreview(
                            order.getId(),
                            receipt.pdfFileName(),
                            delivery.deliveryDate(),
                            true,
                            "Fulfillment details are ready"
                    ))
                    .orTimeout(properties.getResultTimeoutMillis(), TimeUnit.MILLISECONDS)
                    .handle((preview, exception) -> {
                        if (exception == null) {
                            return preview;
                        }

                        log.warn("Fulfillment details for order {} are unavailable", order.getId(), exception);
                        return OrderFulfillmentPreview.unavailable(
                                order.getId(),
                                "Fulfillment details are temporarily unavailable. Please try again later."
                        );
                    });
        } catch (TaskRejectedException exception) {
            log.warn("Async executor rejected fulfillment processing for order {}", order.getId(), exception);
            return CompletableFuture.completedFuture(OrderFulfillmentPreview.unavailable(
                    order.getId(),
                    "Fulfillment processing is busy. Please try again later."
            ));
        }
    }
}
