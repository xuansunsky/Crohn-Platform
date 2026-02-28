package com.xuan.croprogram.model;

import lombok.Data;
import java.util.Date;

@Data
public class DietReport {
    private Long id;
    private Long foodId;
    private Long userId;
    private Integer reactionLevel;
    private String location;
    private String content;
    private String imagesJson;
    private Date createTime;
    private Integer isDeleted;
    private String brand;     // 接前端的 formData.brand
    private String product;   // 接前端的 formData.product
    private Integer level;
}