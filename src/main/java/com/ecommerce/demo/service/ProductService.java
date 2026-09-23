package com.ecommerce.demo.service;

import com.ecommerce.demo.model.Product;

import java.util.List;

public interface ProductService {
    Product create(Product product);
    Product getById(Long id);
    List<Product> getAll();
    Product update(Long id, Product product);
    void delete(Long id);
    Product reduceStock(Long productId, int quantity);
}
