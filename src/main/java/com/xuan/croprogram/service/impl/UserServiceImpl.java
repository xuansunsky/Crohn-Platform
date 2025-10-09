package com.xuan.croprogram.service.impl;

import com.xuan.croprogram.mapper.UserMapper;
import com.xuan.croprogram.model.User;
import com.xuan.croprogram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public boolean registerUser(User user) {
        // 检查手机号是否已注册
        User existingUser = userMapper.findByPhoneNumber(user.getPhoneNumber());
        if (existingUser != null) {
            return false;  // 手机号已存在
        }

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 插入新用户
        userMapper.insertUser(user);
        return true;
    }

    @Override
    public boolean authenticateUser(User user) {
        // 检查手机号是否存在
        User existingUser = userMapper.findByPhoneNumber(user.getPhoneNumber());
        if (existingUser == null) {
            return false;  // 手机号不存在
        }

        // 检查密码是否匹配
        return passwordEncoder.matches(user.getPassword(), existingUser.getPassword());
    }
}
