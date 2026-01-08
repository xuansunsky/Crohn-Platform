package com.xuan.croprogram.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发送者
    private Long senderId;

    // 接收者
    private Long receiverId;

    // 内容 (MySQL里是 TEXT，这里用 String 就行)
    private String content;

    // 类型：TEXT, IMAGE
    private String type;

    // 0未读, 1已读
    private Integer isRead;

    private LocalDateTime createdAt;

    // 自动填充时间
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}