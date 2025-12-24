package com.xuan.croprogram.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MedicalPolicy {
    // 1. 身份锚点
    private Long id;
    private String cityCode; // 对应 city_code
    private String cityName; // 对应 city_name
    private String policyType; // employee 或 resident

    // 2. 核心数据
    private Integer isMente; // 0或1
    private Integer isDual;  // 0或1
    private BigDecimal dualRatio; // 报销比例，用BigDecimal防丢精度
    private Integer threshold; // 起付线
    private BigDecimal ratio;  // 住院比例
    private Integer cap;       // 封顶
    private String dualNote;   // 备注
    private String summary;    // 摘要

    // 3. 防腐层
    private String contributor;
    private Date createTime;
    private Date updateTime;
}
