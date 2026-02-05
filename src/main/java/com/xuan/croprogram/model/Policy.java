package com.xuan.croprogram.model;

import lombok.Data;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField; // 如果你用了MyBatisPlus
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class Policy {
    private Long id;
    private String cityName;
    private String policyType;
    private String updateTime;
    private String nickname;

    private Boolean mente;
    private Boolean dualChannel;

    private Integer deductible;
    private Integer nominalRatio;
    private Integer hiddenSelfPay;
    private Integer dualRatio;
    private String dualNote;
    private String summary;

    // 🔥 1. 这个字段对应数据库 (JSON字符串)
    // @JsonIgnore 意思是：不返给前端，前端不需要看这串乱码
    @JsonIgnore
    private String drugsJson;

    // 🔥 2. 这个字段对应前端 (List对象)
    // @TableField(exist = false) 意思是：数据库没这列，别去查表
    // (如果你用的原生MyBatis，这个注解不用加，只要Mapper里不写它就行)
    @TableField(exist = false)
    private List<DrugItem> drugs;
    // ✨ 新增字段
    private Long userId;        // 谁传的
    private Integer auditStatus;// 0待审, 1已审
    private Integer likes;      // 点赞

    private String evidenceImgs;

    // 📸 证据图片 (前端收发 List)
    @TableField(exist = false)
    private List<String> evidenceList;

    // 内部类：药物结构
    @Data
    public static class DrugItem {
        private String key;
        private String name;
        private String icon;
        private String color;
        private String status;
        private String phone;
        private String comment;
    }
}