package com.xuan.croprogram.model;


import lombok.Data;



import java.time.Instant;

@Data
public class Role {
    private Long id;

    private String roleName;

    private String roleDesc;


    private Instant createTime;

}