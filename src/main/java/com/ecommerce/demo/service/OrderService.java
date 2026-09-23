package com.ecommerce.demo.service;

import com.ecommerce.demo.model.Order;

import java.util.List;

public interface OrderService {
    Order placeOrder(Long userId);
    Order getById(Long id);
    List<Order> getByUser(Long userId);
    Order updateStatus(Long orderId, String status);
    Order cancelOrder(Long orderId);
}
