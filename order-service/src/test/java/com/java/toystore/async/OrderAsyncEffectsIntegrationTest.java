package com.java.toystore.async;

import com.java.toystore.configuration.AsyncConfiguration;
import com.java.toystore.domain.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        AsyncConfiguration.class,
        OrderNotificationService.class,
        OrderReceiptService.class,
        DeliveryEstimateService.class,
        OrderAsyncEffectsIntegrationTest.TestAsyncComponents.class
})
class OrderAsyncEffectsIntegrationTest {

    private final OrderNotificationService orderNotificationService;
    private final OrderReceiptService orderReceiptService;
    private final DeliveryEstimateService deliveryEstimateService;
    private final BlockingAsyncWorkDelay delay;

    @Autowired
    OrderAsyncEffectsIntegrationTest(OrderNotificationService orderNotificationService,
                                     OrderReceiptService orderReceiptService,
                                     DeliveryEstimateService deliveryEstimateService,
                                     BlockingAsyncWorkDelay delay) {
        this.orderNotificationService = orderNotificationService;
        this.orderReceiptService = orderReceiptService;
        this.deliveryEstimateService = deliveryEstimateService;
        this.delay = delay;
    }

    @BeforeEach
    void resetDelay() {
        delay.reset();
    }

    @Test
    void fireAndForgetReturnsBeforeSlowNotificationFinishes() throws Exception {
        delay.blockNextOperations(1);
        long startedAt = System.nanoTime();

        orderNotificationService.sendOrderConfirmation("alice", order(1));

        long returnedInMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        assertThat(returnedInMillis).isLessThan(100);
        assertThat(delay.awaitStarted(1, TimeUnit.SECONDS)).isTrue();
        assertThat(delay.threadNames()).allMatch(name -> name.startsWith("toy-store-async-"));
        delay.release();
    }

    @Test
    void twoIndependentAsyncOperationsStartBeforeEitherIsReleased() throws Exception {
        delay.blockNextOperations(2);

        var receiptFuture = orderReceiptService.generateReceiptPdf(order(5));
        var deliveryFuture = deliveryEstimateService.requestEstimate(order(5));

        assertThat(delay.awaitStarted(1, TimeUnit.SECONDS)).isTrue();
        delay.release();

        OrderReceipt receipt = receiptFuture.get(1, TimeUnit.SECONDS);
        DeliveryEstimate estimate = deliveryFuture.get(1, TimeUnit.SECONDS);
        assertThat(receipt.generatedByThread()).startsWith("toy-store-async-");
        assertThat(estimate.calculatedByThread()).startsWith("toy-store-async-");
    }

    private OrderResponse order(int id) {
        OrderResponse order = new OrderResponse();
        order.setId(id);
        return order;
    }

    @Configuration
    static class TestAsyncComponents {

        @Bean
        BlockingAsyncWorkDelay asyncWorkDelay() {
            return new BlockingAsyncWorkDelay();
        }
    }

    static class BlockingAsyncWorkDelay implements AsyncWorkDelay {

        private final List<String> threadNames = new CopyOnWriteArrayList<>();
        private volatile CountDownLatch started = new CountDownLatch(0);
        private volatile CountDownLatch release = new CountDownLatch(0);

        void reset() {
            threadNames.clear();
            started = new CountDownLatch(0);
            release = new CountDownLatch(0);
        }

        void blockNextOperations(int numberOfOperations) {
            started = new CountDownLatch(numberOfOperations);
            release = new CountDownLatch(1);
        }

        boolean awaitStarted(long timeout, TimeUnit unit) throws InterruptedException {
            return started.await(timeout, unit);
        }

        void release() {
            release.countDown();
        }

        List<String> threadNames() {
            return threadNames;
        }

        @Override
        public void waitForSlowOperation(String operationName) {
            threadNames.add(Thread.currentThread().getName());
            started.countDown();
            try {
                release.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        }
    }
}
