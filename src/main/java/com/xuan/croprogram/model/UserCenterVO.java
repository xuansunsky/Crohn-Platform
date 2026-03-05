package com.xuan.croprogram.model;

import lombok.Data;
import java.util.Date;

@Data
public class UserCenterVO {
    // 来自 user 表
    private String nickname;
    private String avatar;

    // 来自 user_health_profile 表
    private String healthPhase;
    private String dietStrategy;
    private String bowelStatus;
    private String badges; // 徽章 JSON 字符串

    // 后端动态计算的字段，不在数据库实体类里
    private Integer maintainedDays;

    // 隐藏字段：用于后端计算时间，不一定非要传给前端
    private Date phaseStartDate;
}