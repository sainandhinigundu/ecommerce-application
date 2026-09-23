package com.ecommerce.demo.service;

import com.ecommerce.demo.model.User;

import java.util.List;

public interface UserService {
    User register(User user);
    User getById(Long id);
    User getByEmail(String email);
    List<User> getAll();
    User update(Long id, User user);
    void delete(Long id);
}
