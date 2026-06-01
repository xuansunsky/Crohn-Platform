package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 战友状态（每人当前的一条状态）
 */
@Data
public class UserStatus {
    private Long userId;
    private String emoji;
    private String text;
    private String description;
    private String accent;
    // green / yellow / red
    private String zone;
    private LocalDateTime updatedAt;
}
