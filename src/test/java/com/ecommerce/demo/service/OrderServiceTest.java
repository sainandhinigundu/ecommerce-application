package com.ecommerce.demo.service;

import com.ecommerce.demo.exception.ResourceNotFoundException;
import com.ecommerce.demo.model.*;
import com.ecommerce.demo.repository.OrderRepository;
import com.ecommerce.demo.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartService cartService;
    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Cart cart;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice Sharma", "alice@example.com", "secret123", "CUSTOMER");
        product = new Product(1L, "Wireless Mouse", "Ergonomic mouse", new BigDecimal("799.00"), 50);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        CartItem item = new CartItem(1L, cart, product, 2);
        cart.getItems().add(item);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setTotalAmount(new BigDecimal("1598.00"));
        order.setStatus(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Places an order successfully from a non-empty cart")
    void placeOrder_success() {
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        when(productService.reduceStock(1L, 2)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.placeOrder(1L);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("1598.00");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(cartService, times(1)).clearCart(1L);
    }

    @Test
    @DisplayName("Throws IllegalStateException when placing an order with an empty cart")
    void placeOrder_emptyCart_throwsException() {
        Cart emptyCart = new Cart();
        emptyCart.setUser(user);
        when(cartService.getOrCreateCart(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> orderService.placeOrder(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Returns order when found by id")
    void getById_found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getById(1L);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("1598.00");
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when order id does not exist")
    void getById_notFound_throwsException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Returns all orders for a given user")
    void getByUser_returnsList() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(order));

        List<Order> result = orderService.getByUser(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Updates order status to a valid value")
    void updateStatus_valid_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(1L, "shipped");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    @DisplayName("Throws IllegalArgumentException for an invalid status value")
    void updateStatus_invalid_throwsException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, "NOT_A_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Cancels a pending order")
    void cancelOrder_success() {
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancelOrder(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Throws IllegalStateException when cancelling an already delivered order")
    void cancelOrder_alreadyDelivered_throwsException() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }
}
