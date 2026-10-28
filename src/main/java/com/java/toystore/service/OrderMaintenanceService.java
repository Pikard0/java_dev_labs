package com.java.toystore.service;

import com.java.toystore.domain.enums.OrderStatus;
import com.java.toystore.domain.model.Order;
import com.java.toystore.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMaintenanceService {

    private final OrderRepository orderRepository;

    @Transactional
    public int declineStalePlacedOrders(int staleHours, int batchSize) {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(staleHours);
        List<Order> staleOrders = orderRepository
                .findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(OrderStatus.PLACED, cutoff)
                .stream()
                .limit(batchSize)
                .toList();

        staleOrders.forEach(order -> order.setStatus(OrderStatus.DECLINED));
        orderRepository.saveAll(staleOrders);
        log.info("Declined {} stale placed orders older than {} hours", staleOrders.size(), staleHours);
        return staleOrders.size();
    }
}
