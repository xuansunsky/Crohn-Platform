package com.xuan.croprogram.controller;

import com.xuan.croprogram.config.QiniuUtils;
import com.xuan.croprogram.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin // 允许跨域，防止前端报错
public class UploadController {

    @Autowired
    private QiniuUtils qiniuUtils; // 注入工具人

    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {

        // 1. 先防个呆，万一传个空文件
        if (file.isEmpty()) {
            return new ApiResponse<>("文件怎么是空的？", null, 400);
        }

        // 🔥 调用七牛云上传
        String imgUrl = qiniuUtils.upload(file);

        if (imgUrl != null) {
            // 2. 构造返回数据
            Map<String, String> data = new HashMap<>();
            data.put("url", imgUrl);

            // 3. 完美返回：(msg, data, code)
            return new ApiResponse<>("上传成功", data, 200);
        }

        // 4. 失败兜底
        return new ApiResponse<>("上传失败，可能是七牛云那边炸了", null, 500);
    }
}