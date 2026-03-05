package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.UserCenterMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.UserCenterVO;
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

    @GetMapping("/info")
    public ApiResponse<UserHealthProfile> getCenterInfo(@AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getId();
        UserHealthProfile profile = userCenterMapper.getCenterInfo(userId);

        if (profile == null) {
            profile = new UserHealthProfile();
        }

        // 计算天数
        int days = 0;
        if (profile.getPhaseStartDate() != null) {
            long diffInMillis = System.currentTimeMillis() - profile.getPhaseStartDate().getTime();
            days = (int) (diffInMillis / (1000 * 60 * 60 * 24));
        }
        profile.setMaintainedDays(days);

        // 动态推导徽章 (依然建议在后端算，不要存数据库)
        List<String> userBadges = new ArrayList<>();
        userBadges.add("CROHNS_V1");
        if ("无麸质饮食".equals(profile.getDietStrategy())) userBadges.add("GLUTEN_FREE");
        if ("低 FODMAP".equals(profile.getDietStrategy())) userBadges.add("LOW_FODMAP");
        if ("临床缓解期".equals(profile.getHealthPhase()) && days >= 100) userBadges.add("HUNDRED_DAYS_PEACE");
        if (userId.equals(1L)) userBadges.add("ARCHITECT");

        profile.setBadgeCodes(userBadges);

        return new ApiResponse<>("获取成功", profile, 200);
    }

    @PostMapping("/save")
    public ApiResponse<String> saveProfile(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody UserHealthProfile profile
    ) {
        // 覆盖 ID，直接丢给 Mapper 落地
        profile.setUserId(loginUser.getId());
        userCenterMapper.saveOrUpdateHealthProfile(profile);
        return new ApiResponse<>("档案保存成功", null, 200);
    }
    @PostMapping("/update-basic")
    public ApiResponse<String> updateBasicInfo(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody UserHealthProfile profile
    ) {
        // 1. 强行绑定当前用户，防止越权
        profile.setUserId(loginUser.getId());

        // 2. 扔给动态 Mapper，它会自动判断哪些字段有值并拼接 SQL
        userCenterMapper.updateBasicInfo(profile);

        return new ApiResponse<>("资料更新成功", null, 200);
    }
}