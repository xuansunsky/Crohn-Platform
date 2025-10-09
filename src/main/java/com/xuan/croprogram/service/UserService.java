package com.xuan.croprogram.service;

import com.xuan.croprogram.model.User;

public interface UserService {
    boolean registerUser(User user);
    boolean authenticateUser(User user);
}
