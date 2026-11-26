package com.java.toystore.configuration;

import com.java.toystore.async.OrderNotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsyncConfigurationTest {

    @Test
    void executor_usesConfiguredBoundsAndThreadPrefix() {
        AsyncTaskProperties properties = new AsyncTaskProperties();
        properties.setCorePoolSize(1);
        properties.setMaxPoolSize(2);
        properties.setQueueCapacity(3);
        properties.setThreadNamePrefix("toy-store-async-");

        ThreadPoolTaskExecutor executor = new AsyncConfiguration(properties).toyStoreTaskExecutor();
        executor.initialize();
        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(1);
            assertThat(executor.getMaxPoolSize()).isEqualTo(2);
            assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(3);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("toy-store-async-");
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void overloadedExecutor_rejectsExtraTaskExplicitly() throws Exception {
        AsyncTaskProperties properties = new AsyncTaskProperties();
        properties.setCorePoolSize(1);
        properties.setMaxPoolSize(1);
        properties.setQueueCapacity(1);
        ThreadPoolTaskExecutor executor = new AsyncConfiguration(properties).toyStoreTaskExecutor();
        executor.initialize();
        CountDownLatch firstTaskStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstTask = new CountDownLatch(1);

        try {
            executor.execute(() -> {
                firstTaskStarted.countDown();
                try {
                    releaseFirstTask.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
            assertThat(firstTaskStarted.await(1, TimeUnit.SECONDS)).isTrue();

            executor.execute(() -> { });

            assertThatThrownBy(() -> executor.execute(() -> { }))
                    .isInstanceOf(TaskRejectedException.class);
        } finally {
            releaseFirstTask.countDown();
            executor.shutdown();
        }
    }

    @Test
    void uncaughtHandler_acceptsFailureFromVoidAsyncMethod() throws Exception {
        AsyncUncaughtExceptionHandlerHolder handlerHolder = new AsyncUncaughtExceptionHandlerHolder(new AsyncConfiguration(new AsyncTaskProperties()));

        assertThatCode(() -> handlerHolder.handle(new IllegalStateException("mail gateway failed")))
                .doesNotThrowAnyException();
    }

    private record AsyncUncaughtExceptionHandlerHolder(AsyncConfiguration configuration) {

        void handle(Exception exception) throws NoSuchMethodException {
            configuration.getAsyncUncaughtExceptionHandler().handleUncaughtException(
                    exception,
                    OrderNotificationService.class.getMethod("sendOrderConfirmation", String.class,
                            com.java.toystore.domain.dto.OrderResponse.class),
                    "alice", new com.java.toystore.domain.dto.OrderResponse()
            );
        }
    }
}
