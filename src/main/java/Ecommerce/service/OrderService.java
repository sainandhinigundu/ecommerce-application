package Ecommerce.service;

import Ecommerce.model.Order;
import Ecommerce.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order placeOrder(Order order) {
        order.setStatus("PLACED");
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order cancelOrder(Long id) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus("CANCELLED");
            return orderRepository.save(order);
        }).orElse(null);
    }
}