package com.xuan.croprogram.model;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Friendship {
    @TableId(type = IdType.AUTO)
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