package com.xuan.croprogram.controller;


import com.xuan.croprogram.mapper.ExperiencePostMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.ExperiencePost;
import com.xuan.croprogram.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experience")
public class ExperienceController {

    @Autowired
    private ExperiencePostMapper postMapper;

    // 1. 获取所有卡片 (无需登录也能看？还是必须登录？假设必须登录)
    @GetMapping("/list")
    public ApiResponse<List<ExperiencePost>> getAllPosts() {
        return new ApiResponse<>("获取成功", postMapper.findAll(), 200);
    }

    // 2. 发布新卡片
    @PostMapping("/publish")
    public ApiResponse<String> publishPost(
            @AuthenticationPrincipal LoginUser loginUser, // 👈 拿当前登录用户
            @RequestBody ExperiencePost post
    ) {
        // 强制绑定为当前用户的 ID
        post.setUserId(loginUser.getUserId());

        postMapper.insert(post);
        return new ApiResponse<>("发布成功！", null, 200);
    }

    // 3. 删除卡片
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        // 先查出来这个帖子
        ExperiencePost post = postMapper.findById(id);
        if (post == null) {
            return new ApiResponse<>("帖子不存在", null, 404);
        }

        // 🛡️ 鉴权：只有“管理员”或者“作者本人”能删
        boolean isAdmin = (loginUser.getRoleId()==1);
        boolean isAuthor = post.getUserId().equals(loginUser.getUserId());

        if (!isAdmin && !isAuthor) {
            return new ApiResponse<>("你没有权限删除别人的勋章！", null, 403);
        }

        postMapper.deleteById(id);
        return new ApiResponse<>("删除成功", null, 200);
    }
    // 4. 编辑/更新卡片
    @PostMapping("/update")
    public ApiResponse<String> updatePost(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody ExperiencePost post // 前端传过来的新数据
    ) {
        // 1. 既然是更新，ID 必须得有
        if (post.getId() == null) {
            return new ApiResponse<>("更新失败：缺少帖子ID", null, 400);
        }

        // 2. 先查旧数据（为了验身）
        ExperiencePost existingPost = postMapper.findById(post.getId());
        if (existingPost == null) {
            return new ApiResponse<>("帖子不存在或已被删除", null, 404);
        }

        // 3. 🛡️ 鉴权：只有“管理员”或者“作者本人”能改
        // 你的逻辑：roleId == 1 是管理员
        boolean isAdmin = (loginUser.getRoleId() == 1);
        // 注意：数据库查出来的 userId 是 Long 类型，比较要用 equals
        boolean isAuthor = existingPost.getUserId().equals(loginUser.getUserId());

        if (!isAdmin && !isAuthor) {
            return new ApiResponse<>("你没有权限修改别人的记忆！", null, 403);
        }

        // 4. 准备更新
        // ⚠️ 关键点：我们只更新内容，不准改作者 ID 和 创建时间
        // 虽然 Mapper SQL 里没写 update userId，但为了保险，这里不动 existingPost 的敏感字段

        // 直接调用 mapper 更新
        postMapper.update(post);

        return new ApiResponse<>("修改成功", null, 200);
    }
}