package com.xuan.croprogram.model;

import lombok.Data;
import java.util.Date;

@Data
public class Food {
    private Long id;
    private String brandName;
    private String foodName;
    private String coverImg;
    private String summaryDesc;
    private String tagsJson;

    private Integer totalVotes = 0;
    private Integer safeRate = 0;

    private Integer level1Votes = 0;
    private Integer level2Votes = 0;
    private Integer level3Votes = 0;
    private Integer level4Votes = 0;
    private Integer level5Votes = 0;
    private Integer level6Votes = 0;

    private Date createTime;
    private Date updateTime;
}