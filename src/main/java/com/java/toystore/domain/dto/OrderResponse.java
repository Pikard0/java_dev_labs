package com.java.toystore.domain.dto;

import com.java.toystore.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private List<OrderItemResponse> items;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private OffsetDateTime createdAt;
}
