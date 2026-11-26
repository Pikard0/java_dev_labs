package com.java.toystore.catalog;

import com.java.toystore.messaging.OrderPlacedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPlacedEventConsumerTest {

    @Test
    void duplicateEventIsProcessedOnlyOnce() {
        ProcessedOrderEventStore eventStore = new ProcessedOrderEventStore();
        OrderPlacedEventConsumer consumer = new OrderPlacedEventConsumer(eventStore);
        OrderPlacedEvent event = new OrderPlacedEvent(
                UUID.randomUUID(),
                11,
                "alice",
                new BigDecimal("29.90"),
                OffsetDateTime.now()
        );

        consumer.consume(event);
        consumer.consume(event);

        assertThat(eventStore.size()).isOne();
        assertThat(eventStore.findAll()).containsExactly(event);
    }
}
