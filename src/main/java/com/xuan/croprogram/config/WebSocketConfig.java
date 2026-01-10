package com.xuan.croprogram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSocket
public class WebSocketConfig {

    /**
     * 这个 Bean 会自动注册使用了 @ServerEndpoint 注解声明的 WebSocket endpoint
     * 简单说：它是负责接线的大爷
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}