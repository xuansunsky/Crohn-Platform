package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.TeamStatusMapper;
import com.xuan.croprogram.mapper.UserVerificationMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.StatusView;
import com.xuan.croprogram.model.UserVerification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 战友状态墙：每人一条状态 + 无声关心 + 微信扫码转款
 * 资金完全不经平台，只读取用户自愿上传的收款码图片链接。
 */
@RestController
@RequestMapping("/api/team/status")
public class TeamStatusController {

    @Autowired
    private TeamStatusMapper teamStatusMapper;

    @Autowired
    private UserVerificationMapper verificationMapper;

    /**
     * 状态墙：返回我自己 + 所有好友的当前状态。
     * 未通过 IBD 认证的用户看不到战友隐私（members 返回空，前端展示认证门槛）。
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> list(@AuthenticationPrincipal LoginUser loginUser) {
        Long myId = loginUser.getUserId();
        StatusView me = teamStatusMapper.findMyStatus(myId);

        UserVerification myVerify = verificationMapper.findByUser(myId);
        String verifyStatus = myVerify == null ? "NONE" : myVerify.getStatus();
        boolean approved = "APPROVED".equals(verifyStatus);

        Map<String, Object> data = new HashMap<>();
        data.put("me", me);
        data.put("myVerifyStatus", verifyStatus);
        data.put("myUnlocked", approved);

        // 状态墙现在对所有人可见（基础状态不再隐藏）；战友详细资料/隐私在 profile 接口里再做门槛。
        List<StatusView> members = teamStatusMapper.findFriendStatuses(myId);
        for (StatusView m : members) {
            m.setReactors(teamStatusMapper.findReactors(m.getUserId()));
        }
        data.put("members", members);
        return new ApiResponse<>("获取成功", data, 200);
    }

    /**
     * 战友资料卡：状态 + 是否上传病例 + 你们之间的送温暖往来。
     * 查看资料需自己先上传病例解锁（myUnlocked=false 时前端引导上传）。
     */
    @GetMapping("/profile/{userId}")
    public ApiResponse<Map<String, Object>> profile(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long userId
    ) {
        Long myId = loginUser.getUserId();

        UserVerification myVerify = verificationMapper.findByUser(myId);
        boolean myUnlocked = myVerify != null && "APPROVED".equals(myVerify.getStatus());

        StatusView target = teamStatusMapper.findUserStatusFor(userId, myId);
        target.setReactors(teamStatusMapper.findReactors(userId));

        Map<String, Object> data = new HashMap<>();
        data.put("user", target);
        data.put("myUnlocked", myUnlocked);
        data.put("iSent", teamStatusMapper.countReactionsTotal(myId, userId));
        data.put("theySent", teamStatusMapper.countReactionsTotal(userId, myId));
        data.put("hasPayCode", teamStatusMapper.findPayCode(userId) != null);
        return new ApiResponse<>("获取成功", data, 200);
    }

    /**
     * 设置我的状态：{ emoji, text, description, accent, zone }
     */
    @PostMapping("/set")
    public ApiResponse<String> set(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> req
    ) {
        String text = req.get("text");
        if (text == null || text.trim().isEmpty()) {
            return new ApiResponse<>("状态文案不能为空", null, 400);
        }
        String zone = req.getOrDefault("zone", "green");
        teamStatusMapper.upsertStatus(
                loginUser.getUserId(),
                req.getOrDefault("emoji", "🙂"),
                text.trim(),
                req.getOrDefault("description", ""),
                req.getOrDefault("accent", "slate"),
                zone
        );
        return new ApiResponse<>("状态已更新", null, 200);
    }

    /**
     * 给某位战友一个无声关心：{ targetUserId, reactionType }
     */
    @PostMapping("/react")
    public ApiResponse<String> react(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, Object> req
    ) {
        Object tid = req.get("targetUserId");
        if (tid == null) {
            return new ApiResponse<>("缺少目标用户", null, 400);
        }
        Long targetUserId = Long.parseLong(tid.toString());
        Long myId = loginUser.getUserId();
        if (teamStatusMapper.countReactionToday(targetUserId, myId) > 0) {
            return new ApiResponse<>("今天已经给 TA 送过关心啦，明天再来 🌙", null, 409);
        }
        String type = req.get("reactionType") == null ? "seen" : req.get("reactionType").toString();
        teamStatusMapper.insertReaction(targetUserId, myId, type);
        return new ApiResponse<>("已送达关心", null, 200);
    }

    /**
     * 读取某用户的微信收款码图片链接（供扫码转账）
     */
    @GetMapping("/paycode/{userId}")
    public ApiResponse<String> getPayCode(@PathVariable Long userId) {
        return new ApiResponse<>("获取成功", teamStatusMapper.findPayCode(userId), 200);
    }

    /**
     * 设置我的微信收款码：{ url }
     * 只保存图片链接，绝不保存银行卡号、身份证等隐私信息。
     */
    @PostMapping("/paycode")
    public ApiResponse<String> setPayCode(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> req
    ) {
        String url = req.get("url");
        if (url == null || url.trim().isEmpty()) {
            return new ApiResponse<>("收款码链接不能为空", null, 400);
        }
        teamStatusMapper.upsertPayCode(loginUser.getUserId(), url.trim());
        return new ApiResponse<>("收款码已保存", null, 200);
    }
}
