package com.xuan.croprogram.controller;

import com.xuan.croprogram.config.CosService;
import com.xuan.croprogram.model.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api") // 你可以根据你的习惯改成 /diet 等
public class UploadController {

    @Autowired
    private CosService cosService;

    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        String url = cosService.uploadImage(file);

        if (url != null) {
            return new ApiResponse<>("上传成功", url, 200);
        }
        return new ApiResponse<>("上传失败", null, 500);
    }
}