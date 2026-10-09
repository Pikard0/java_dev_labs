package com.java.carparts.controller;

import com.java.carparts.domain.dto.OrderResponse;
import com.java.carparts.domain.dto.PlaceOrderRequest;
import com.java.carparts.domain.enums.OrderStatus;
import com.java.carparts.security.CurrentUser;
import com.java.carparts.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                     @AuthenticationPrincipal CurrentUser user) {
        return orderService.placeOrder(user.username(), request);
    }

    @GetMapping("/mine")
    public List<OrderResponse> myOrders(@AuthenticationPrincipal CurrentUser user) {
        return orderService.listMyOrders(user.username());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Integer id, @AuthenticationPrincipal CurrentUser user) {
        return orderService.getOrder(id, user.username(), user.hasRole("ADMIN"));
    }

    @GetMapping
    public List<OrderResponse> allOrders(@AuthenticationPrincipal CurrentUser user) {
        if (!user.hasRole("ADMIN")) throw new org.springframework.security.access.AccessDeniedException("Admin only");
        return orderService.listAllOrders();
    }

    @PatchMapping("/{id}/status")
    
    public OrderResponse updateStatus(@PathVariable Integer id, @RequestParam OrderStatus status,
                                      @AuthenticationPrincipal CurrentUser user) {
        if (!user.hasRole("ADMIN")) throw new org.springframework.security.access.AccessDeniedException("Admin only");
        return orderService.updateStatus(id, status);
    }
}
