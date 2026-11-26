package com.java.toystore.catalog;

import com.java.toystore.messaging.OrderPlacedEvent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ProcessedOrderEventStore {

    private final ConcurrentMap<UUID, OrderPlacedEvent> processedEvents = new ConcurrentHashMap<>();

    public boolean recordIfNew(OrderPlacedEvent event) {
        return processedEvents.putIfAbsent(event.eventId(), event) == null;
    }

    public List<OrderPlacedEvent> findAll() {
        return processedEvents.values().stream()
                .sorted(Comparator.comparing(OrderPlacedEvent::occurredAt))
                .toList();
    }

    public int size() {
        return processedEvents.size();
    }
}
