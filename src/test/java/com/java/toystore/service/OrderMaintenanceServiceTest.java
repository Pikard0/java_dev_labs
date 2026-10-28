package com.java.toystore.service;

import com.java.toystore.domain.enums.OrderStatus;
import com.java.toystore.domain.model.Order;
import com.java.toystore.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMaintenanceServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Test
    void repeatedExecution_isIdempotentBecauseOnlyPlacedOrdersAreChanged() {
        Order order = new Order();
        order.setId(12);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(OffsetDateTime.now().minusHours(30));
        when(orderRepository.findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                org.mockito.ArgumentMatchers.eq(OrderStatus.PLACED), any(OffsetDateTime.class)))
                .thenReturn(List.of(order), List.of());
        OrderMaintenanceService service = new OrderMaintenanceService(orderRepository);

        int firstRun = service.declineStalePlacedOrders(24, 10);
        int secondRun = service.declineStalePlacedOrders(24, 10);

        assertThat(firstRun).isEqualTo(1);
        assertThat(secondRun).isZero();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DECLINED);
        verify(orderRepository, times(2)).saveAll(any());
    }

    @Test
    void batchSize_limitsTheNumberOfUpdatedOrders() {
        Order first = placedOrder(1);
        Order second = placedOrder(2);
        when(orderRepository.findByStatusAndCreatedAtBeforeOrderByCreatedAtAsc(
                org.mockito.ArgumentMatchers.eq(OrderStatus.PLACED), any(OffsetDateTime.class)))
                .thenReturn(List.of(first, second));
        OrderMaintenanceService service = new OrderMaintenanceService(orderRepository);

        int processed = service.declineStalePlacedOrders(24, 1);

        assertThat(processed).isEqualTo(1);
        assertThat(first.getStatus()).isEqualTo(OrderStatus.DECLINED);
        assertThat(second.getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    private Order placedOrder(int id) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(OffsetDateTime.now().minusDays(2));
        return order;
    }
}
