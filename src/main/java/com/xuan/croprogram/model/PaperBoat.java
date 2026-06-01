package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaperBoat {
    private Long id;
    private String content;
    private Integer breezeCount;
    private LocalDateTime createdAt;
    
    // 辅助字段
    private String time = "刚刚";
    private Boolean breezed = false;
}
