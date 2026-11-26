package com.java.toystore.catalog;

import com.java.toystore.domain.dto.ToyResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final ProcessedOrderEventStore eventStore;

    @GetMapping("/health")
    public String health() {
        return "catalog-service is running";
    }

    @GetMapping("/toys")
    public List<ToyResponse> listToys() {
        return List.of(
                new ToyResponse(1, "TOY-001", "Wooden train", "vehicles", "Toy Store", "3+", new BigDecimal("24.90"), 12),
                new ToyResponse(2, "TOY-002", "Building blocks", "construction", "Toy Store", "5+", new BigDecimal("19.50"), 8)
        );
    }

    @GetMapping("/order-events")
    public List<com.java.toystore.messaging.OrderPlacedEvent> processedOrderEvents() {
        return eventStore.findAll();
    }
}
