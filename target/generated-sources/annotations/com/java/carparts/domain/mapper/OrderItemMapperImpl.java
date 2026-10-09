package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.OrderItemResponse;
import com.java.carparts.domain.model.OrderItem;
import com.java.carparts.domain.model.Product;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-09T01:04:10+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.19 (Microsoft)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItemResponse toResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse orderItemResponse = new OrderItemResponse();

        orderItemResponse.setProductId( orderItemProductId( orderItem ) );
        orderItemResponse.setProductName( orderItemProductName( orderItem ) );
        orderItemResponse.setQuantity( orderItem.getQuantity() );
        orderItemResponse.setUnitPrice( orderItem.getUnitPrice() );

        orderItemResponse.setLineTotal( orderItem.lineTotal() );

        return orderItemResponse;
    }

    private Integer orderItemProductId(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }

    private String orderItemProductName(OrderItem orderItem) {
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getName();
    }
}
