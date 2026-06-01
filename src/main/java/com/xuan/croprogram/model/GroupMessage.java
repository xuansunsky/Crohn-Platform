package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupMessage {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String content;
    private String type;
    private LocalDateTime createdAt;
    // from JOIN with users
    private String senderName;
    private String senderAvatar;
}
