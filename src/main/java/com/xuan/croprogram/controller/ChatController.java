package com.xuan.croprogram.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xuan.croprogram.mapper.FriendshipMapper;
import com.xuan.croprogram.mapper.MessageMapper;
import com.xuan.croprogram.mapper.UserMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.FriendDto;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.Message;
import com.xuan.croprogram.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// 🔥 1. 引入刚才写的 WebSocketServer

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1. 发送消息
     * 前端传：{ "receiverId": 102, "content": "你好呀", "type": "text" }
     */
    @PostMapping("/send")
    public ApiResponse<String> sendMessage(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Message message
    ) {
        // 补全发送者 ID (就是我)
        message.setSenderId(loginUser.getUserId());

        // 默认类型 text
        if (message.getType() == null) {
            message.setType("text");
        }
        // 入库
        messageMapper.insert(message);

        // 统一 JSON 信封推送，前端按 kind 路由
        User sender = userMapper.findByPhoneNumber(loginUser.getPhoneNumber());
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("kind", "single");
        envelope.put("senderId", loginUser.getUserId());
        envelope.put("senderName", sender == null ? "" : sender.getNickname());
        envelope.put("senderAvatar", sender == null ? null : sender.getAvatar());
        envelope.put("content", message.getContent());
        envelope.put("type", message.getType());
        try {
            WebSocketServer.sendInfo(message.getReceiverId(), objectMapper.writeValueAsString(envelope));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ApiResponse<>("发送成功", null, 200);
    }

    /**
     * 群发广播：{ "content": "大家注意按时吃药！" }
     * 给我所有好友各发一条单聊消息。
     */
    @PostMapping("/broadcast")
    public ApiResponse<String> broadcast(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody Map<String, String> req
    ) {
        Long myId = loginUser.getUserId();
        String content = req.get("content");
        if (content == null || content.trim().isEmpty()) {
            return new ApiResponse<>("广播内容不能为空", null, 400);
        }

        List<FriendDto> friends = friendshipMapper.findMyFriends(myId);
        if (friends.isEmpty()) {
            return new ApiResponse<>("你还没有好友，先去加几个战友吧", null, 400);
        }

        User sender = userMapper.findByPhoneNumber(loginUser.getPhoneNumber());
        for (FriendDto f : friends) {
            Message m = new Message();
            m.setSenderId(myId);
            m.setReceiverId(f.getFriendId());
            m.setContent(content);
            m.setType("text");
            messageMapper.insert(m);

            Map<String, Object> envelope = new HashMap<>();
            envelope.put("kind", "single");
            envelope.put("senderId", myId);
            envelope.put("senderName", sender == null ? "" : sender.getNickname());
            envelope.put("senderAvatar", sender == null ? null : sender.getAvatar());
            envelope.put("content", content);
            envelope.put("type", "text");
            try {
                WebSocketServer.sendInfo(f.getFriendId(), objectMapper.writeValueAsString(envelope));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ApiResponse<>("已广播给 " + friends.size() + " 位战友 📡", null, 200);
    }

    /**
     * 2. 获取我和某人的聊天记录
     * URL: GET /api/chat/history?friendId=102
     */
    @GetMapping("/history")
    public ApiResponse<List<Message>> getChatHistory(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam Long friendId
    ) {
        Long myId = loginUser.getUserId();
        List<Message> history = messageMapper.findChatHistory(myId, friendId);

        return new ApiResponse<>("获取成功", history, 200);
    }
}