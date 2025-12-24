package com.xuan.croprogram.controller;

import com.xuan.croprogram.config.JwtUtil;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.User;
import com.xuan.croprogram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil; // ✅ 注入JWT工具类

    // 用户注册
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody User user) {
        boolean isRegistered = userService.registerUser(user);
        return isRegistered
                ? new ApiResponse<>("注册成功！", null, 200)
                : new ApiResponse<>("注册失败，手机号已存在！", null, 400);
    }

    // 用户登录
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody User user) {
        boolean isValid = userService.authenticateUser(user);
        if (isValid) {
            String token = jwtUtil.generateToken(user.getPhoneNumber()); // ✅ 生成JWT
            return new ApiResponse<>("登录成功！", token, 200);
        } else {
            return new ApiResponse<>("手机号或密码错误！", null, 401);
        }
    }
    @GetMapping("/whoami")
    public String whoAmI() {
        // 从 Spring Security 上下文里抓取当前登录的人
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 如果 Filter 工作正常，这里应该能打印出你的用户名
        // 如果没工作，这里可能是 "anonymousUser"
        return "后端收到了！你的身份是: " + auth.getName();
    }
}
