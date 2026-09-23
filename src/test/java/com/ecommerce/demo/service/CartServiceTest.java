package com.ecommerce.demo.service;

import com.ecommerce.demo.exception.InsufficientStockException;
import com.ecommerce.demo.exception.ResourceNotFoundException;
import com.ecommerce.demo.model.*;
import com.ecommerce.demo.repository.CartItemRepository;
import com.ecommerce.demo.repository.CartRepository;
import com.ecommerce.demo.repository.ProductRepository;
import com.ecommerce.demo.repository.UserRepository;
import com.ecommerce.demo.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cart Service Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Alice Sharma", "alice@example.com", "secret123", "CUSTOMER");
        product = new Product(1L, "Wireless Mouse", "Ergonomic mouse", new BigDecimal("799.00"), 50);
        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
    }

    @Test
    @DisplayName("Returns existing cart when one already exists for the user")
    void getOrCreateCart_existingCart_returnsIt() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCart(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Creates a new cart when none exists for the user")
    void getOrCreateCart_noExistingCart_createsNew() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.getOrCreateCart(1L);

        assertThat(result.getUser().getId()).isEqualTo(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when creating a cart for an unknown user")
    void getOrCreateCart_unknownUser_throwsException() {
        when(cartRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getOrCreateCart(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Adds a new item to the cart")
    void addItem_newItem_success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addItem(1L, 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("Increases quantity when the same product is added again")
    void addItem_existingProduct_increasesQuantity() {
        CartItem existing = new CartItem(1L, cart, product, 2);
        cart.getItems().add(existing);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addItem(1L, 1L, 3);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Throws InsufficientStockException when adding more than available stock")
    void addItem_insufficientStock_throwsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 999))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when quantity is zero or negative")
    void addItem_invalidQuantity_throwsException() {
        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Updates the quantity of an existing cart item")
    void updateItemQuantity_success() {
        CartItem existing = new CartItem(1L, cart, product, 2);
        cart.getItems().add(existing);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.updateItemQuantity(1L, 1L, 10);

        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when updating a non-existent cart item")
    void updateItemQuantity_itemNotFound_throwsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 55L, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Removes an item from the cart")
    void removeItem_success() {
        CartItem existing = new CartItem(1L, cart, product, 2);
        cart.getItems().add(existing);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.removeItem(1L, 1L);

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when removing a non-existent cart item")
    void removeItem_itemNotFound_throwsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.removeItem(1L, 55L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Clears all items from the cart")
    void clearCart_success() {
        cart.getItems().add(new CartItem(1L, cart, product, 2));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.clearCart(1L);

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository, times(1)).save(cart);
    }
}
