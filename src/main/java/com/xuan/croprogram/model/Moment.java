package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Moment {
    private Long id;
    private Long userId;
    private String content;
    private String imagesJson;
    private String device;
    private String location;
    private String visibility; // public(公开) / comrade(仅战友) / private(仅自己)
    private Integer likesCount;
    private LocalDateTime createdAt;
    // from JOIN with users
    private String nickname;
    private String avatar;
    // set in controller per request
    private Boolean liked;
    // 每条动态带上评论列表
    private List<MomentComment> comments;
}
