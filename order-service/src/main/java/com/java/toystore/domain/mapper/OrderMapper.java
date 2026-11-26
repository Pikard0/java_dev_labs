package com.java.toystore.domain.mapper;

import com.java.toystore.domain.dto.OrderResponse;
import com.java.toystore.domain.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderResponse toResponse(Order order);
}
