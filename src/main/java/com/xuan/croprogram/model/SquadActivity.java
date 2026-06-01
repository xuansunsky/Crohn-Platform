package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SquadActivity {
    private Long id;
    private Long groupId;
    private Long userId;
    private String action;
    private LocalDateTime createdAt;
    // from JOIN with users
    private String nickname;
}
