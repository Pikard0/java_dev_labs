package com.java.carparts.service;

import com.java.carparts.domain.dto.OrderItemRequest;
import com.java.carparts.domain.dto.OrderResponse;
import com.java.carparts.domain.dto.PlaceOrderRequest;
import com.java.carparts.aop.AuditAction;
import com.java.carparts.domain.enums.OrderStatus;
import com.java.carparts.domain.mapper.OrderMapper;
import com.java.carparts.domain.model.Order;
import com.java.carparts.domain.model.OrderItem;
import com.java.carparts.domain.model.Product;
import com.java.carparts.exception.InsufficientStockException;
import com.java.carparts.exception.OrderNotFoundException;
import com.java.carparts.exception.ProductNotFoundException;
import com.java.carparts.repository.OrderRepository;
import com.java.carparts.repository.ProductRepository;
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

    private final ProductRepository productRepository;
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
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));

            assertSufficientStock(product, itemRequest.getQuantity());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(unitPrice);
            order.addItem(item);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
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

    private void assertSufficientStock(Product product, int requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(product.getId(), requestedQuantity, product.getStock());
        }
    }

}
