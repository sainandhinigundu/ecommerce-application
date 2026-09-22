package Ecommerce.service;

import Ecommerce.model.Cart;
import Ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public Cart addToCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public List<Cart> getCartByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart updateQuantity(Long id, int quantity) {
        return cartRepository.findById(id).map(cart -> {
            cart.setQuantity(quantity);
            return cartRepository.save(cart);
        }).orElse(null);
    }

    public void removeFromCart(Long id) {
        cartRepository.deleteById(id);
    }
}