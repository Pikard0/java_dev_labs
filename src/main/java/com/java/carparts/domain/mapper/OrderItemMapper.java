package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.OrderItemResponse;
import com.java.carparts.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "lineTotal", expression = "java(orderItem.lineTotal())")
    OrderItemResponse toResponse(OrderItem orderItem);
}
