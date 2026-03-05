package com.xuan.croprogram.model;

import lombok.Data;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class UserProfileUpdateDTO {
    // === 1. users 表的基础信息 ===
    private String nickname;
    private String avatar;

    // === 2. user_health_profile 表的业务信息 ===

    // 加上时间格式化注解，防止前端传过来的日期后端解析报错
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date diagnosedAt; // 确诊日期

    private String healthPhase; // 阶段：如 临床缓解期

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date phaseStartDate; // 阶段开始日期

    private String dietStrategy; // 饮食
    private String bowelStatus;  // 肠道
}
