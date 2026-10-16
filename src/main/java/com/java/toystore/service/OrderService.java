package com.java.toystore.service;

import com.java.toystore.domain.dto.OrderItemRequest;
import com.java.toystore.domain.dto.OrderResponse;
import com.java.toystore.domain.dto.PlaceOrderRequest;
import com.java.toystore.aop.AuditAction;
import com.java.toystore.domain.enums.OrderStatus;
import com.java.toystore.domain.mapper.OrderMapper;
import com.java.toystore.domain.model.Order;
import com.java.toystore.domain.model.OrderItem;
import com.java.toystore.domain.model.Toy;
import com.java.toystore.exception.InsufficientStockException;
import com.java.toystore.exception.OrderNotFoundException;
import com.java.toystore.exception.ToyNotFoundException;
import com.java.toystore.repository.OrderRepository;
import com.java.toystore.repository.ToyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ToyRepository toyRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse placeOrder(String customerUsername, PlaceOrderRequest request) {
        Order order = new Order();
        order.setCustomerUsername(customerUsername);
        order.setCreatedAt(OffsetDateTime.now());
        order.setStatus(OrderStatus.PLACED);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Toy toy = toyRepository.findById(itemRequest.getToyId())
                    .orElseThrow(() -> new ToyNotFoundException(itemRequest.getToyId()));

            assertSufficientStock(toy, itemRequest.getQuantity());

            BigDecimal unitPrice = toy.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            OrderItem item = new OrderItem();
            item.setToy(toy);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            order.addItem(item);

            toy.setStock(toy.getStock() - itemRequest.getQuantity());
            toyRepository.save(toy);
        }

        order.setTotalPrice(total);

        order.setStatus(OrderStatus.CONFIRMED);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listMyOrders(String customerUsername) {
        return orderRepository.findByCustomerUsername(customerUsername).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Integer orderId, String requestingUsername, boolean isAdmin) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!isAdmin && !order.getCustomerUsername().equals(requestingUsername)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        }

        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listAllOrders() {
        return orderRepository.findAllWithItems().stream().map(orderMapper::toResponse).toList();
    }

    @Transactional
    @AuditAction("Order status changed")
    public OrderResponse updateStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void assertSufficientStock(Toy toy, int requestedQuantity) {
        if (toy.getStock() < requestedQuantity) {
            throw new InsufficientStockException(toy.getId(), requestedQuantity, toy.getStock());
        }
    }

}
