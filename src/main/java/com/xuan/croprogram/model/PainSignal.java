package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PainSignal {
    private Long id;
    private Long userId;
    private String location;
    private Integer status;
    private LocalDateTime createdAt;

    // 辅助显示字段
    private String name;
    private String avatar;
    private String dist = "同城战友";
    private String sign = "正在遭受病痛折磨，深呼吸中... 🩹";
    private Boolean warmed = false;
}
