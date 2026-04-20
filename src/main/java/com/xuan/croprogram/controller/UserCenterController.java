package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.UserCenterMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.UserHealthProfile;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/center")
public class UserCenterController {

    @Resource
    private UserCenterMapper userCenterMapper;
    /**
     * 获取当前用户的健康档案和基础信息
     */
    @GetMapping("/info")
    public ApiResponse<UserHealthProfile> getProfileInfo(@AuthenticationPrincipal LoginUser loginUser) {
        // 去查数据库
        UserHealthProfile profile = userCenterMapper.getProfileByUserId(loginUser.getUserId());

        // 【防御性编程】如果他是个刚注册的新用户，还没档案，给个空的防止前端报错
        if (profile == null) {
            profile = new UserHealthProfile();
            profile.setUserId(loginUser.getUserId());
         
        }

        return new ApiResponse<>("获取档案成功", profile, 200);
    }
    @PostMapping("/save")
    public ApiResponse<String> saveProfile(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody UserHealthProfile profile
    ) {
        // 覆盖 ID，直接丢给 Mapper 落地
        profile.setUserId(loginUser.getUserId());
        userCenterMapper.saveOrUpdateHealthProfile(profile);
        return new ApiResponse<>("档案保存成功", null, 200);
    }
    @PostMapping("/update-basic")
    public ApiResponse<String> updateBasicInfo(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody UserHealthProfile profile
    ) {
        // 1. 强行绑定当前用户，防止越权
        profile.setUserId(loginUser.getUserId());

        // 2. 扔给动态 Mapper，它会自动判断哪些字段有值并拼接 SQL
        userCenterMapper.updateBasicInfo(profile);

        return new ApiResponse<>("资料更新成功", null, 200);
    }
    /**
     * 1. 批量装备徽章 (全息舱点击保存后调用)
     * 前端传参示例: { "badges": "[\"克罗恩 V1 认证\", \"全栈架构师\"]" }
     */
    @PostMapping("/update-badges")
    public ApiResponse<String> updateBadges(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody UserHealthProfile profile
    ) {
        // 1. 强行绑定当前登录人的 ID，防越权
        profile.setUserId(loginUser.getUserId());

        // 2. 打印日志是个好习惯，方便你排查前端传过来的 JSON 对不对
        System.out.println("[雷达系统] 用户ID: " + loginUser.getUserId() + " 准备装备徽章: " + profile.getBadges());

        // 3. 调用 Mapper，直接把前端传来的 JSON 字符串更新到数据库
        userCenterMapper.updateBadges(profile);

        // 4. 返回完美响应
        return new ApiResponse<>("荣誉徽章装备成功", null, 200);
    }

    /**
     * 2. 无感秒改状态 (矩阵控制台单点调用)
     * 前端传参示例: { "healthPhase": "临床缓解期" } 或者 { "dietStrategy": "低FODMAP" }
     */
    @PostMapping("/update-status")
    public ApiResponse<String> updateDynamicStatus(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody UserHealthProfile profile
    ) {
        profile.setUserId(loginUser.getUserId());
        userCenterMapper.updateDynamicStatus(profile);
        return new ApiResponse<>("状态参数同步成功", null, 200);
    }
}