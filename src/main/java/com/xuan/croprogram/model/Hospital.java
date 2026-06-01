package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Hospital {
    private Long id;
    private String name;
    private String level;
    private String region;
    private Double rating;
    private Integer recommendRate;
    private Boolean greenChannel;
    private String responseTime;
    private String scopeWait;
    private String mreWait;
    private String difficulty;
    private Boolean injectionRoom;
    private String techsJson;
    private String summary;
    private String doctorsJson;
    private String tipsJson;
    private LocalDateTime createdAt;
}
