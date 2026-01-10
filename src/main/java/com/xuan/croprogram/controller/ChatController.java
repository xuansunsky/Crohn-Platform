package com.xuan.croprogram.controller;

import com.xuan.croprogram.mapper.MessageMapper;
import com.xuan.croprogram.model.ApiResponse;
import com.xuan.croprogram.model.LoginUser;
import com.xuan.croprogram.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
// 🔥 1. 引入刚才写的 WebSocketServer
import com.xuan.croprogram.controller.WebSocketServer;
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private MessageMapper messageMapper;

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
        message.setSenderId(loginUser.getId());

        // 默认类型 text
        if (message.getType() == null) {
            message.setType("text");
        }
        // 入库
        messageMapper.insert(message);
        // 简单版推送：直接把内容发过去
        WebSocketServer.sendInfo(message.getReceiverId(), message.getContent());
        return new ApiResponse<>("发送成功", null, 200);
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
        Long myId = loginUser.getId();
        List<Message> history = messageMapper.findChatHistory(myId, friendId);

        return new ApiResponse<>("获取成功", history, 200);
    }
}