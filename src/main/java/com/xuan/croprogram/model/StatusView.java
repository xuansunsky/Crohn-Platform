package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 状态墙的一行：用户信息 + 当前状态 + 收到的关心数
 */
@Data
public class StatusView {
    private Long userId;
    private String nickname;
    private String avatar;
    private String emoji;
    private String text;
    private String description;
    private String accent;
    private String zone;
    private LocalDateTime updatedAt;
    private Integer reactions;
    // 我今天是否已经给 TA 送过关心（1=送过）
    private Integer reactedToday;
    // 是否已通过 IBD 认证（战友标识）
    private Integer verified;
    // 最近送关心的人（昵称+头像）
    private List<Map<String, Object>> reactors;
}
