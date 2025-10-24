package com.xuan.croprogram.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private  JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头中取出 Authorization 信息
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 没有token 或 格式不对（不是Bearer开头），直接放行（让Security自己处理）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取token内容（去掉"Bearer "）
        jwt = authHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);

        // 如果当前没有认证信息 且 token 合法
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 验证token有效性
            if (jwtUtil.validateToken(jwt, username)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username, null, null // 可以放用户权限列表
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 设置到Spring Security的上下文
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 放行请求
        filterChain.doFilter(request, response);
    }
}
