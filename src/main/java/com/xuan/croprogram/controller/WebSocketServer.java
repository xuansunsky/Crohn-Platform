package com.xuan.croprogram.controller;

import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监听路径：ws://localhost:8080/ws/{userId}
 * 前端连接的时候，要带上自己的 userId，这样后端才知道是谁连上来了
 */
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    // 1. 存所有在线用户的电话本 (线程安全)
    // Key: userId, Value: session (连接会话)
    private static ConcurrentHashMap<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    // 2. 建立连接：当用户进来时触发
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        // 把这个人的连接存起来
        onlineUsers.put(userId, session);
        System.out.println("用户 " + userId + " 上线了！当前在线人数: " + onlineUsers.size());
    }

    // 3. 关闭连接：当用户刷新页面或关闭时触发
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        onlineUsers.remove(userId);
        System.out.println("用户 " + userId + " 下线了。");
    }

    // 4. 收到消息：前端发来消息时触发 (虽然我们主要用 HTTP 发，但这也能发)
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("收到消息: " + message);
        // 这里暂时不处理，我们主要用它来“推”消息
    }

    // 5. 发生错误
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 🔥 核心功能：发送消息给指定用户
     * static 方法，方便在 ChatController 里直接调用
     */
    public static void sendInfo(Long receiverId, String message) {
        // 1. 查电话本，看这个人在不在线
        Session session = onlineUsers.get(receiverId);

        // 2. 如果在线，直接发过去
        if (session != null && session.isOpen()) {
            try {
                // 发送文本消息
                session.getBasicRemote().sendText(message);
                System.out.println("推送消息给 " + receiverId + " 成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("用户 " + receiverId + " 不在线，消息已存库，下次上线自取。");
        }
    }
}