package com.xuan.croprogram.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class UserHealthProfile {
    // === 数据库映射 & 基础信息 ===
    private Long userId;
    private String nickname;
    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date diagnosedAt;

    private String healthPhase;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date phaseStartDate;

    private String dietStrategy;
    private String bowelStatus;

    // === 纯业务计算字段（只出不进） ===
    // Access.READ_ONLY 保证前端传了这个字段也会被后端忽略，只能由后端发给前端
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer maintainedDays;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> badgeCodes;
}