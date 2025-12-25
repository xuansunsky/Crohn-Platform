package com.xuan.croprogram.model;

import lombok.Data;
import lombok.Getter;


@Data
public class ApiResponse<T> {

    // Getters 和 Setters
    private String message;
    private T data;
    private int status;

    // 构造方法
    public ApiResponse(String message, T data, int status) {
        this.message = message;
        this.data = data;
        this.status = status;
    }


}
