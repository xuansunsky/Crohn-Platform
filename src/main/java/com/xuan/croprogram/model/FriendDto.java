package com.xuan.croprogram.model;
import lombok.Data;
@Data
public class FriendDto {
    // 1. 对方的个人信息 (从 Users 表拿)
    private Long friendId;     // 对方的用户ID (注意：不是 requesterId，而是对方的ID)
    private String nickname;   // 对方名字
    private String avatar;     // 对方头像

    // 2. 我们的关系信息 (从 Friendships 表拿)
    private Long friendshipId; // 关系记录ID (用来删除或同意)
    private String status;     // 状态

    // 3. 聊天相关 (给列表页展示用的)
    private String lastMsg;    // 最后一条消息
    private String lastTime;   // 最后聊天时间
    private Integer unread;    // 未读消息数
}