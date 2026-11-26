package com.java.toystore.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class OrderPlacedEventPublisherTest {

    @Test
    void publishesEventToBrokerWithConfiguredRoute() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        OrderPlacedEventPublisher publisher = new OrderPlacedEventPublisher(
                rabbitTemplate,
                "toy-store.order-events",
                "order.placed"
        );
        OrderPlacedEvent event = event();

        publisher.publishAfterCommit(event);

        verify(rabbitTemplate).convertAndSend(
                eq("toy-store.order-events"),
                eq("order.placed"),
                eq(event),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void publishesOnlyAfterTransactionCommit() throws NoSuchMethodException {
        TransactionalEventListener listener = OrderPlacedEventPublisher.class
                .getMethod("publishAfterCommit", OrderPlacedEvent.class)
                .getAnnotation(TransactionalEventListener.class);

        assertThat(listener).isNotNull();
        assertThat(listener.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }

    private OrderPlacedEvent event() {
        return new OrderPlacedEvent(
                UUID.randomUUID(),
                11,
                "alice",
                new BigDecimal("29.90"),
                OffsetDateTime.now()
        );
    }
}
