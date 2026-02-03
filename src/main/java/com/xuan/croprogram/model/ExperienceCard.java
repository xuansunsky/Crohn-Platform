package com.xuan.croprogram.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
// import com.baomidou.mybatisplus.annotation.*; // 如果是 MyBatis-Plus

@Data
@Table(name = "experience_card") // 对应数据库表名
public class ExperienceCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 图标 (Emoji)
     */
    private String icon;

    /**
     * 皮肤 (neon, sunset...)
     */
    private String theme;

    /**
     * 标签
     * 说明：数据库里存的是 "治愈,心情"，这里我们先映射成String
     * 在 Controller 层再处理 List<String> 和 String 的转换
     */
    private String tags;

    /**
     * 创建时间
     * 前端那个 2025.12.23 就用这个时间自动格式化
     */
    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;
}