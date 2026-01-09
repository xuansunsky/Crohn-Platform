package com.xuan.croprogram.model;


import lombok.AllArgsConstructor;
import lombok.Data;

// 这是一个单纯的数据载体，用来在 Filter 和 Controller 之间传值
@Data
@AllArgsConstructor
public class LoginUser {
    private Long id;
    private String phoneNumber;
    private Long roleId;

}