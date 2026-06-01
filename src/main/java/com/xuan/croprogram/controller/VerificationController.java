package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.UserVerificationMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.UserVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * IBD 病友身份认证：上传证明 → 后台审核 → 通过后亮"战友"标识、可看战友隐私
 */
@RestController
@RequestMapping("/api/verify")
public class VerificationController {

    @Autowired
    private UserVerificationMapper verificationMapper;

    /**
     * 提交认证：{ proofImageUrl }
     */
    @PostMapping("/submit")
    public ApiResponse<String> submit(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> req
    ) {
        String url = req.get("proofImageUrl");
        if (url == null || url.trim().isEmpty()) {
            return new ApiResponse<>("请先上传证明图片", null, 400);
        }
        verificationMapper.submit(loginUser.getUserId(), url.trim());
        return new ApiResponse<>("已上传，战友权限已解锁 🛡️", null, 200);
    }

    /**
     * 我的认证状态
     */
    @GetMapping("/me")
    public ApiResponse<UserVerification> myStatus(@AuthenticationPrincipal LoginUser loginUser) {
        return new ApiResponse<>("获取成功", verificationMapper.findByUser(loginUser.getUserId()), 200);
    }

    /**
     * 待审核列表（仅管理员 roleId==1）
     */
    @GetMapping("/pending")
    public ApiResponse<List<UserVerification>> pending(@AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser.getRoleId() == null || loginUser.getRoleId() != 1) {
            return new ApiResponse<>("无权限", null, 403);
        }
        return new ApiResponse<>("获取成功", verificationMapper.findPending("PENDING"), 200);
    }

    /**
     * 审核：{ userId, approve: true/false }（仅管理员）
     */
    @PostMapping("/review")
    public ApiResponse<String> review(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> req
    ) {
        if (loginUser.getRoleId() == null || loginUser.getRoleId() != 1) {
            return new ApiResponse<>("无权限", null, 403);
        }
        Object uid = req.get("userId");
        if (uid == null) {
            return new ApiResponse<>("缺少用户ID", null, 400);
        }
        boolean approve = Boolean.TRUE.equals(req.get("approve")) || "true".equals(String.valueOf(req.get("approve")));
        verificationMapper.review(Long.parseLong(uid.toString()), approve ? "APPROVED" : "REJECTED");
        return new ApiResponse<>(approve ? "已通过认证" : "已驳回", null, 200);
    }
}
