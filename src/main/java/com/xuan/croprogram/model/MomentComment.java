package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MomentComment {
    private Long id;
    private Long momentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    // from JOIN with users
    private String nickname;
    private String avatar;
}
