package com.java.toystore.controller;

import com.java.toystore.async.OrderFulfillmentPreview;
import com.java.toystore.domain.dto.OrderResponse;
import com.java.toystore.domain.dto.PlaceOrderRequest;
import com.java.toystore.domain.enums.OrderStatus;
import com.java.toystore.security.CurrentUser;
import com.java.toystore.validation.AdminEndpoint;
import com.java.toystore.web.CurrentUserParam;
import com.java.toystore.service.OrderService;
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
                                     @CurrentUserParam CurrentUser user) {
        return orderService.placeOrder(user.username(), request);
    }

    @GetMapping("/mine")
    public List<OrderResponse> myOrders(@CurrentUserParam CurrentUser user) {
        return orderService.listMyOrders(user.username());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Integer id, @CurrentUserParam CurrentUser user) {
        return orderService.getOrder(id, user.username(), user.hasRole("ADMIN"));
    }

    @GetMapping("/{id}/fulfillment-preview")
    public OrderFulfillmentPreview getFulfillmentPreview(@PathVariable Integer id,
                                                         @CurrentUserParam CurrentUser user) {
        return orderService.getFulfillmentPreview(id, user.username(), user.hasRole("ADMIN"));
    }

    @GetMapping
    @AdminEndpoint
    public List<OrderResponse> allOrders(@CurrentUserParam CurrentUser user) {
        return orderService.listAllOrders();
    }

    @PatchMapping("/{id}/status")
    @AdminEndpoint
    public OrderResponse updateStatus(@PathVariable Integer id, @RequestParam OrderStatus status,
                                      @CurrentUserParam CurrentUser user) {
        return orderService.updateStatus(id, status);
    }
}
