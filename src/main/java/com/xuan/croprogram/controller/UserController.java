package com.xuan.croprogram.controller;

import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.User;
import com.xuan.croprogram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) {
        boolean isRegistered = userService.registerUser(user);
        if (isRegistered) {
            return new ApiResponse<>("注册成功！", null, 200);
        } else {
            return new ApiResponse<>("注册失败，手机号已存在！", null, 400);
        }
    }

    // 用户登录
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody User user) {
        boolean isValid = userService.authenticateUser(user);
        if (isValid) {
            return new ApiResponse<>("登录成功！", null, 200);
        } else {
            return new ApiResponse<>("手机号或密码错误！", null, 401);
        }
    }
}
