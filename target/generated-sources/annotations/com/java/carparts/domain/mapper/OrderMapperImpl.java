package com.java.carparts.domain.mapper;

import com.java.carparts.domain.dto.OrderItemResponse;
import com.java.carparts.domain.dto.OrderResponse;
import com.java.carparts.domain.model.Order;
import com.java.carparts.domain.model.OrderItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-09T01:04:10+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.19 (Microsoft)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse orderResponse = new OrderResponse();

        orderResponse.setId( order.getId() );
        orderResponse.setItems( orderItemListToOrderItemResponseList( order.getItems() ) );
        orderResponse.setTotalPrice( order.getTotalPrice() );
        orderResponse.setStatus( order.getStatus() );
        orderResponse.setCreatedAt( order.getCreatedAt() );

        return orderResponse;
    }

    protected List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( orderItemMapper.toResponse( orderItem ) );
        }

        return list1;
    }
}
