package com.xuan.croprogram.model;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {

    private Long id;

    // 手机号
    private String phoneNumber;

    // 加密后的密码
    private String password;

    // 昵称
    private String nickname;

    // 性别：0未知 1男 2女
    private Integer gender;

    // 确诊日期
    private LocalDate diagnosedAt;

    // 生日（可选）
    private LocalDate birthday;
    //头像
    private String avatar;
    // 城市
    private String city;

    // 角色：USER / ADMIN
    private Long roleId;

    // 是否有效
    private Boolean isActive = true;

    // 创建时间
    private LocalDateTime createdAt;

    // 更新时间
    private LocalDateTime updatedAt;

    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
