package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.OrderItemResponse;
import com.java.carparts.domain.dto.OrderResponse;
import com.java.carparts.domain.enums.OrderStatus;
import com.java.carparts.domain.model.Order;
import com.java.carparts.domain.model.OrderItem;
import com.java.carparts.domain.model.Product;
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

    private Product product(int id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    @Test
    void orderItemMapper_mapsProductFieldsAndComputesLineTotal() {
        OrderItem item = new OrderItem();
        item.setProduct(product(1, "Brake Pad"));
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("10.00"));

        OrderItemResponse response = orderItemMapper.toResponse(item);

        assertThat(response.getProductId()).isEqualTo(1);
        assertThat(response.getProductName()).isEqualTo("Brake Pad");
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
        item.setProduct(product(2, "Oil Filter"));
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("25.00"));
        order.addItem(item);

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(5);
        assertThat(response.getTotalPrice()).isEqualByComparingTo("50.00");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("Oil Filter");
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
