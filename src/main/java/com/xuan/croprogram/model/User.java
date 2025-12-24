package com.xuan.croprogram.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Table(name = "users")  // 显式指定表名
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 手机号
    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // 加密后的密码
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    // 昵称
    @Column(name = "nick_name", nullable = false, length = 50)
    private String nickName;

    // 性别：0未知 1男 2女
    @Column(name = "gender")
    private Integer gender;

    // 确诊日期
    @Column(name = "diagnosed_at")
    private LocalDate diagnosedAt;

    // 生日（可选）
    @Column(name = "birthday")
    private LocalDate birthday;

    // 城市
    @Column(name = "city", length = 50)
    private String city;

    // 角色：USER / ADMIN
    @Column(name = "role", length = 20)
    private String role = "USER";

    // 是否有效
    @Column(name = "is_active")
    private Boolean isActive = true;

    // 创建时间
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 更新时间
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
