package com.ecommerce.demo.service;

import com.ecommerce.demo.model.Cart;

public interface CartService {
    Cart getOrCreateCart(Long userId);
    Cart addItem(Long userId, Long productId, int quantity);
    Cart updateItemQuantity(Long userId, Long cartItemId, int quantity);
    Cart removeItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
