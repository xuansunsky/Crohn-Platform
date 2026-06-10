package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatGroup {
    private Long id;
    private String name;
    private String avatar;
    private String notice;
    private Long ownerId;
    private LocalDateTime createdAt;
    // 列表展示用：成员数（非表字段）
    private Integer memberCount;
    // 列表展示用：最近一条消息（非表字段）
    private String lastMsg;
    private String lastTime;
}
