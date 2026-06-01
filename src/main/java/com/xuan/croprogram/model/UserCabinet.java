package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserCabinet {
    private Long id;
    private Long userId;
    private String drugName;
    private String drugIcon;
    private String dosage;
    private String frequency;
    private String timeOfDay;
    private LocalDateTime createdAt;
}
