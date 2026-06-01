package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;
    // from JOIN with users
    private String nickname;
    private String avatar;
}
