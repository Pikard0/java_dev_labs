package com.java.toystore.async;

import com.java.toystore.configuration.AsyncTaskProperties;
import com.java.toystore.domain.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderFulfillmentServiceTest {

    @Mock
    private OrderReceiptService orderReceiptService;

    @Mock
    private DeliveryEstimateService deliveryEstimateService;

    @Mock
    private AsyncTaskProperties properties;

    private OrderResponse order() {
        OrderResponse order = new OrderResponse();
        order.setId(7);
        return order;
    }

    @Test
    void buildPreview_combinesResultsFromBothIndependentOperations() throws Exception {
        OrderResponse order = order();
        when(properties.getResultTimeoutMillis()).thenReturn(500L);
        when(orderReceiptService.generateReceiptPdf(order)).thenReturn(CompletableFuture.completedFuture(
                new OrderReceipt(7, "order-7.pdf", "toy-store-async-1")
        ));
        when(deliveryEstimateService.requestEstimate(order)).thenReturn(CompletableFuture.completedFuture(
                new DeliveryEstimate(7, LocalDate.of(2026, 10, 30), "toy-store-async-2")
        ));

        OrderFulfillmentPreview preview = new OrderFulfillmentService(
                orderReceiptService, deliveryEstimateService, properties
        ).buildPreview(order).get(1, TimeUnit.SECONDS);

        assertThat(preview.available()).isTrue();
        assertThat(preview.receiptFileName()).isEqualTo("order-7.pdf");
        assertThat(preview.estimatedDeliveryDate()).isEqualTo(LocalDate.of(2026, 10, 30));
    }

    @Test
    void buildPreview_failedAsyncOperation_returnsFallbackInsteadOfPropagatingError() throws Exception {
        OrderResponse order = order();
        when(properties.getResultTimeoutMillis()).thenReturn(500L);
        when(orderReceiptService.generateReceiptPdf(order)).thenReturn(CompletableFuture.failedFuture(
                new IllegalStateException("PDF service is unavailable")
        ));
        when(deliveryEstimateService.requestEstimate(order)).thenReturn(CompletableFuture.completedFuture(
                new DeliveryEstimate(7, LocalDate.now(), "toy-store-async-2")
        ));

        OrderFulfillmentPreview preview = new OrderFulfillmentService(
                orderReceiptService, deliveryEstimateService, properties
        ).buildPreview(order).get(1, TimeUnit.SECONDS);

        assertThat(preview.available()).isFalse();
        assertThat(preview.message()).contains("temporarily unavailable");
    }

    @Test
    void buildPreview_takesTooLong_returnsFallbackAfterConfiguredTimeout() throws Exception {
        OrderResponse order = order();
        when(properties.getResultTimeoutMillis()).thenReturn(20L);
        when(orderReceiptService.generateReceiptPdf(order)).thenReturn(new CompletableFuture<>());
        when(deliveryEstimateService.requestEstimate(order)).thenReturn(new CompletableFuture<>());

        long startedAt = System.nanoTime();
        OrderFulfillmentPreview preview = new OrderFulfillmentService(
                orderReceiptService, deliveryEstimateService, properties
        ).buildPreview(order).get(1, TimeUnit.SECONDS);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        assertThat(preview.available()).isFalse();
        assertThat(elapsedMillis).isLessThan(500);
    }
}
