package com.xuan.croprogram.model;

import jakarta.persistence.*;
import lombok.Data;

import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Data
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "role_name", nullable = false, length = 20)
    private String roleName;

    @Column(name = "role_desc", length = 50)
    private String roleDesc;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_time")
    private Instant createTime;

}