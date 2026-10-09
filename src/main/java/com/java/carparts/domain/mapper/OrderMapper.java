package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.OrderResponse;
import com.java.carparts.domain.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderResponse toResponse(Order order);
}
