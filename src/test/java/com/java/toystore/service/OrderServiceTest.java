package com.java.toystore.service;

import com.java.toystore.async.OrderFulfillmentPreview;
import com.java.toystore.async.OrderFulfillmentService;
import com.java.toystore.async.OrderNotificationService;
import com.java.toystore.configuration.AsyncTaskProperties;
import com.java.toystore.domain.dto.OrderItemRequest;
import com.java.toystore.domain.dto.OrderResponse;
import com.java.toystore.domain.dto.PlaceOrderRequest;
import com.java.toystore.domain.enums.OrderStatus;
import com.java.toystore.domain.mapper.OrderMapper;
import com.java.toystore.domain.model.Order;
import com.java.toystore.domain.model.Toy;
import com.java.toystore.exception.InsufficientStockException;
import com.java.toystore.exception.OrderNotFoundException;
import com.java.toystore.exception.ToyNotFoundException;
import com.java.toystore.repository.OrderRepository;
import com.java.toystore.repository.ToyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ToyRepository toyRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderNotificationService orderNotificationService;

    @Mock
    private OrderFulfillmentService orderFulfillmentService;

    @Mock
    private AsyncTaskProperties asyncTaskProperties;
    
    @InjectMocks
    private OrderService orderService;

    private static final String USERNAME = "alice";

    private Toy toy(int id, String price, int stock) {
        Toy toy = new Toy();
        toy.setId(id);
        toy.setSku("SKU-" + id);
        toy.setName("Toy " + id);
        toy.setCategory("TOYS");
        toy.setBrand("LEGO");
        toy.setPrice(new BigDecimal(price));
        toy.setStock(stock);
        return toy;
    }

    @Test
    void placeOrder_singleItem_belowBulkThreshold_noDiscountApplied() {
        Toy toy = toy(1, "10.00", 50);
        when(toyRepository.findById(1)).thenReturn(Optional.of(toy));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> new OrderResponse());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest(1, 3)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        orderService.placeOrder(USERNAME, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();

        assertThat(saved.getTotalPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(saved.getCustomerUsername()).isEqualTo(USERNAME);
        assertThat(saved.getItems()).hasSize(1);
        verify(orderNotificationService).sendOrderConfirmation(eq(USERNAME), any(OrderResponse.class));
    }

    @ParameterizedTest
    @CsvSource({
            "4,  40.00",
            "5,  50.00",
            "10, 100.00"
    })
    void placeOrder_anyQuantity_usesSimplePriceTimesQuantity(int quantity, String expectedTotal) {
        Toy toy = toy(1, "10.00", 100);
        when(toyRepository.findById(1)).thenReturn(Optional.of(toy));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> new OrderResponse());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest(1, quantity)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        orderService.placeOrder(USERNAME, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        assertThat(orderCaptor.getValue().getTotalPrice()).isEqualByComparingTo(new BigDecimal(expectedTotal));
    }

    @Test
    void placeOrder_multipleItems_totalIsSumOfLineTotals() {
        Toy toyA = toy(1, "10.00", 50);
        Toy toyB = toy(2, "20.00", 50);
        when(toyRepository.findById(1)).thenReturn(Optional.of(toyA));
        when(toyRepository.findById(2)).thenReturn(Optional.of(toyB));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> new OrderResponse());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(
                new OrderItemRequest(1, 2),
                new OrderItemRequest(2, 1)
        ));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        orderService.placeOrder(USERNAME, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        assertThat(orderCaptor.getValue().getTotalPrice()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(orderCaptor.getValue().getItems()).hasSize(2);
    }

    @Test
    void placeOrder_decrementsStockAndSavesEachToy() {
        Toy toy = toy(1, "10.00", 50);
        when(toyRepository.findById(1)).thenReturn(Optional.of(toy));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> new OrderResponse());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest(1, 7)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        orderService.placeOrder(USERNAME, request);

        assertThat(toy.getStock()).isEqualTo(43);
        verify(toyRepository).save(toy);
    }

    @Test
    void placeOrder_snapshotsUnitPriceOnItem_independentOfLaterToyPriceChanges() {
        Toy toy = toy(1, "15.50", 20);
        when(toyRepository.findById(1)).thenReturn(Optional.of(toy));
        when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> new OrderResponse());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest(1, 1)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        orderService.placeOrder(USERNAME, request);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        assertThat(orderCaptor.getValue().getItems().get(0).getUnitPrice())
                .isEqualByComparingTo(new BigDecimal("15.50"));
    }

    @Test
    void placeOrder_toyDoesNotExist_throwsToyNotFoundException() {
        when(toyRepository.findById(99)).thenReturn(Optional.empty());

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest(99, 1)));

        assertThatThrownBy(() -> orderService.placeOrder(USERNAME, request))
                .isInstanceOf(ToyNotFoundException.class);

        verifyNoInteractions(orderRepository);
    }

    @Test
    void placeOrder_insufficientStock_throwsInsufficientStockException() {
        Toy toy = toy(1, "10.00", 2);
        when(toyRepository.findById(1)).thenReturn(Optional.of(toy));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of(new OrderItemRequest(1, 5)));

        assertThatThrownBy(() -> orderService.placeOrder(USERNAME, request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 5")
                .hasMessageContaining("available 2");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void listMyOrders_delegatesToRepositoryAndMapsResults() {
        Order order = new Order();
        OrderResponse response = new OrderResponse();
        when(orderRepository.findByCustomerUsername(USERNAME)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        List<OrderResponse> result = orderService.listMyOrders(USERNAME);

        assertThat(result).containsExactly(response);
    }

    @Test
    void listMyOrders_noOrders_returnsEmptyList() {
        when(orderRepository.findByCustomerUsername(USERNAME)).thenReturn(List.of());

        assertThat(orderService.listMyOrders(USERNAME)).isEmpty();
    }

    @Test
    void getOrder_ownerRequestingOwnOrder_succeeds() {
        Order order = new Order();
        order.setCustomerUsername(USERNAME);
        OrderResponse response = new OrderResponse();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrder(1, USERNAME, false);

        assertThat(result).isSameAs(response);
    }

    @Test
    void getOrder_adminRequestingSomeoneElsesOrder_succeeds() {
        Order order = new Order();
        order.setCustomerUsername("someone-else");
        OrderResponse response = new OrderResponse();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrder(1, "admin-user", true);

        assertThat(result).isSameAs(response);
    }

    @Test
    void getOrder_nonOwnerNonAdmin_throwsAccessDenied() {
        Order order = new Order();
        order.setCustomerUsername("someone-else");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrder(1, USERNAME, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(404, USERNAME, false))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void getFulfillmentPreview_neverCompletingFuture_returnsTimeoutFallback() {
        Order order = new Order();
        order.setCustomerUsername(USERNAME);
        OrderResponse response = new OrderResponse();
        response.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);
        when(orderFulfillmentService.buildPreview(response)).thenReturn(new CompletableFuture<>());
        when(asyncTaskProperties.getResultTimeoutMillis()).thenReturn(10L);

        OrderFulfillmentPreview preview = orderService.getFulfillmentPreview(1, USERNAME, false);

        assertThat(preview.available()).isFalse();
        assertThat(preview.message()).contains("taking too long");
    }
}
