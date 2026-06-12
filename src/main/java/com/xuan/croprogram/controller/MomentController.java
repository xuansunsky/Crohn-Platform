package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.MomentMapper;
import com.xuan.croprogram.mapper.FriendshipMapper;
import com.xuan.croprogram.mapper.GroupMapper;
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

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private GroupMapper groupMapper;

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

    @GetMapping("/user/{targetId}")
    public ApiResponse<List<Moment>> userMoments(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long targetId
    ) {
        Long viewerId = loginUser.getUserId();
        boolean isSelf = viewerId.equals(targetId);
        boolean isFriend = friendshipMapper.countAcceptedRelation(viewerId, targetId) > 0;
        boolean isSameGroup = groupMapper.countSharedGroups(viewerId, targetId) > 0;
        int canSeeFriend = (isSelf || isFriend) ? 1 : 0;
        int canSeeGroup = (!isSelf && !isFriend && isSameGroup) ? 1 : 0;

        List<Moment> moments = momentMapper.findUserVisible(viewerId, targetId, canSeeFriend, canSeeGroup);
        moments.forEach(m -> {
            m.setLiked(momentMapper.checkLiked(m.getId(), viewerId) > 0);
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

    @PostMapping("/update/{id}")
    public ApiResponse<String> update(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Moment request
    ) {
        Moment moment = momentMapper.findById(id);
        if (moment == null) return new ApiResponse<>("动态不存在", null, 404);
        if (!moment.getUserId().equals(loginUser.getUserId()))
            return new ApiResponse<>("只能编辑自己的动态", null, 403);

        String content = request.getContent() == null ? "" : request.getContent().trim();
        String imagesJson = request.getImagesJson();
        if (content.isEmpty() && (imagesJson == null || imagesJson.trim().isEmpty())) {
            return new ApiResponse<>("写点什么或加张图吧", null, 400);
        }

        moment.setContent(content);
        moment.setImagesJson(imagesJson);
        moment.setLocation(request.getLocation());
        moment.setVisibility(request.getVisibility());

        int updated = momentMapper.updateByOwner(moment);
        if (updated <= 0) return new ApiResponse<>("保存失败", null, 400);
        return new ApiResponse<>("已保存", null, 200);
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
