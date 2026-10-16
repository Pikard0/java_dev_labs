package com.java.toystore.domain.mapper;

import com.java.toystore.domain.dto.OrderItemResponse;
import com.java.toystore.domain.dto.OrderResponse;
import com.java.toystore.domain.enums.OrderStatus;
import com.java.toystore.domain.model.Order;
import com.java.toystore.domain.model.OrderItem;
import com.java.toystore.domain.model.Toy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderMapperImpl.class, OrderItemMapperImpl.class})
class OrderMapperImplTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderMapper orderMapper;

    private Toy toy(int id, String name) {
        Toy toy = new Toy();
        toy.setId(id);
        toy.setName(name);
        return toy;
    }

    @Test
    void orderItemMapper_mapsToyFieldsAndComputesLineTotal() {
        OrderItem item = new OrderItem();
        item.setToy(toy(1, "Building Blocks"));
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("10.00"));

        OrderItemResponse response = orderItemMapper.toResponse(item);

        assertThat(response.getToyId()).isEqualTo(1);
        assertThat(response.getToyName()).isEqualTo("Building Blocks");
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getUnitPrice()).isEqualByComparingTo("10.00");
        assertThat(response.getLineTotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void orderMapper_mapsOrderWithNestedItemsList() {
        Order order = new Order();
        order.setId(5);
        order.setTotalPrice(new BigDecimal("50.00"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"));

        OrderItem item = new OrderItem();
        item.setToy(toy(2, "Fashion Doll"));
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));
        order.addItem(item);

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(5);
        assertThat(response.getTotalPrice()).isEqualByComparingTo("50.00");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getToyName()).isEqualTo("Fashion Doll");
        assertThat(response.getItems().get(0).getLineTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void orderMapper_emptyItemsList_mapsToEmptyList() {
        Order order = new Order();
        order.setId(6);
        order.setTotalPrice(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(OffsetDateTime.now());

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.getItems()).isEmpty();
    }
}
