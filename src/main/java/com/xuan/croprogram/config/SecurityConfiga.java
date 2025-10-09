package com.xuan.croprogram.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 明确开启Web安全功能
public class SecurityConfiga {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // **【关键第一步】**: 禁用CSRF保护。对于API来说，这是必须的。
                .csrf(csrf -> csrf.disable())

                // **【关键第二步】**: 配置URL的授权规则
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/users/register", "/api/users/login").permitAll() // 允许所有人访问注册和登录接口
                                .anyRequest().authenticated() // 其他所有请求都需要认证
                )

                // **【可选但推荐】**: 配置会话管理为“无状态(STATELESS)”
                // 因为我们之后会用JWT，所以不需要服务器来管理session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
