package com.xuan.croprogram.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Drug {
    private Long id;
    private String name;
    private String type;
    private String typeLabel;
    private String icon;
    private String tag;
    private Integer safetyRate;
    private Integer responseRate;
    private String company;
    private String tagsJson;
    private String description;
    private String mechanism;
    private Integer priceOriginal;
    private Integer priceReimbursed;
    private String sideEffects;
    private LocalDateTime createdAt;
}
