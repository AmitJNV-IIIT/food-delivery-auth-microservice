package com.sapient.auth_service.service;

import com.sapient.auth_service.modal.User;

public interface UserService {
    public User registerUser(User user);
    public User findByEmail(String email);
}
