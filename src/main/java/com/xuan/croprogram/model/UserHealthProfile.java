package com.xuan.croprogram.model; // ⚠️ 注意：这里改成你自己的真实包名！

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserHealthProfile {

    // === 核心主键 ===
    private Long userId; // 既是主键，也是关联 ID

    // === 从 users 表 JOIN 进来的字段（不在本表存储） ===
    private String nickname;
    private String avatar;

    // === 档案核心字段 ===
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date diagnosedAt;

    private String healthPhase;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date phaseStartDate;

    private String dietStrategy;
    private String bowelStatus;

    // === 存储字段：JSON 字符串 ===
    // 数据库存这个："[\"克罗恩 V1 认证\", \"全栈架构师\"]"
    private String badges;

    // 雷达相关
    private String radarTags;
    private String radarSign;

    // === 纯业务计算字段（只出不进） ===

    // 1. 动态计算维持天数
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getMaintainedDays() {
        if (this.phaseStartDate == null) return 0;
        long diffInMs = Math.abs(new Date().getTime() - this.phaseStartDate.getTime());
        return (int) (diffInMs / (1000 * 60 * 60 * 24));
    }

    // 2. 将数据库的 String 变成前端要的 List (使用 SpringBoot 自带的 Jackson)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<String> getBadgeCodes() {
        if (this.badges == null || this.badges.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.badges, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            System.err.println("解析徽章JSON失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 3. 将雷达标签的 String 变成前端要的 List
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public List<String> getRadarTagList() {
        if (this.radarTags == null || this.radarTags.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.radarTags, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            System.err.println("解析雷达标签JSON失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}