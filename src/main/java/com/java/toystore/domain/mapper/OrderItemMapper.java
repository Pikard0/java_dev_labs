package com.java.toystore.domain.mapper;

import com.java.toystore.domain.dto.OrderItemResponse;
import com.java.toystore.domain.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "toyId", source = "toy.id")
    @Mapping(target = "toyName", source = "toy.name")
    @Mapping(target = "lineTotal", expression = "java(orderItem.lineTotal())")
    OrderItemResponse toResponse(OrderItem orderItem);
}
