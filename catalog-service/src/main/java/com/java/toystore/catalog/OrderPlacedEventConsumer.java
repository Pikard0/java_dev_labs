package com.java.toystore.catalog;

import com.java.toystore.messaging.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedEventConsumer {

    private final ProcessedOrderEventStore eventStore;

    @RabbitListener(queues = "${toy-store.messaging.order-placed-queue}")
    public void consume(OrderPlacedEvent event) {
        if (eventStore.recordIfNew(event)) {
            log.info("Processed order placed event {} for order {}", event.eventId(), event.orderId());
        } else {
            log.info("Ignored duplicate order placed event {}", event.eventId());
        }
    }
}
