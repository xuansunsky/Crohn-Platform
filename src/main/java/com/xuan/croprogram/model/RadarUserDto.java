package com.xuan.croprogram.model; // 注意改成你自己的真实包名！

import lombok.Data;

import java.util.List;
@Data
public class RadarUserDto {
    private Long id;
    private String name;
    private String avatar;
    private String distance;
    private String sign;
    private String tags; // 先用 String 存标签，简单点
}