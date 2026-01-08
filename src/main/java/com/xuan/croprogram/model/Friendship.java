package com.xuan.croprogram.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friendships")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 申请人ID
    private Long requesterId;

    // 接收人ID
    private Long addresseeId;

    // 状态：PENDING / ACCEPTED / REJECTED
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}