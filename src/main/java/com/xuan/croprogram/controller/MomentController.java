package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.MomentMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.Moment;
import com.xuan.croprogram.model.MomentComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moment")
public class MomentController {

    @Autowired
    private MomentMapper momentMapper;

    @GetMapping("/list")
    public ApiResponse<List<Moment>> list(@AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        int viewerVerified = momentMapper.isVerified(userId);
        List<Moment> moments = momentMapper.findVisible(userId, viewerVerified);
        moments.forEach(m -> {
            m.setLiked(momentMapper.checkLiked(m.getId(), userId) > 0);
            m.setComments(momentMapper.findCommentsByMoment(m.getId()));
        });
        return new ApiResponse<>("获取成功", moments, 200);
    }

    @PostMapping("/comment")
    public ApiResponse<String> comment(@AuthenticationPrincipal LoginUser loginUser, @RequestBody MomentComment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return new ApiResponse<>("评论内容不能为空", null, 400);
        }
        comment.setUserId(loginUser.getUserId());
        momentMapper.insertComment(comment);
        return new ApiResponse<>("评论成功", null, 200);
    }

    @PostMapping("/publish")
    public ApiResponse<String> publish(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Moment moment) {
        moment.setUserId(loginUser.getUserId());
        momentMapper.insert(moment);
        return new ApiResponse<>("发布成功", null, 200);
    }

    @PostMapping("/like/{id}")
    public ApiResponse<String> like(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        if (momentMapper.checkLiked(id, userId) > 0) {
            momentMapper.deleteLike(id, userId);
            momentMapper.decrementLikes(id);
            return new ApiResponse<>("已取消点赞", null, 200);
        }
        momentMapper.insertLike(id, userId);
        momentMapper.incrementLikes(id);
        return new ApiResponse<>("点赞成功", null, 200);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        Moment moment = momentMapper.findById(id);
        if (moment == null) return new ApiResponse<>("动态不存在", null, 404);
        if (!moment.getUserId().equals(loginUser.getUserId()) && loginUser.getRoleId() != 1)
            return new ApiResponse<>("无权限", null, 403);
        momentMapper.deleteById(id);
        return new ApiResponse<>("删除成功", null, 200);
    }
}
