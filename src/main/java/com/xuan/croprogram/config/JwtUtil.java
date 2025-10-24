package com.xuan.croprogram.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
@Component
public class JwtUtil {

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 密钥，可以从配置文件中读取，确保其保密性

    // 生成 JWT Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // 设置 Token 的主体（通常是用户名或用户ID）
                .setIssuedAt(new Date())  // 设置发行时间
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 设置过期时间（1小时）
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 使用 HMAC 签名算法和密钥
                .compact();
    }

    // 提取用户名（或其他信息）从 JWT Token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 提取签名部分的 claim 信息
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)  // 设置密钥
                .build()
                .parseClaimsJws(token)  // 解析 token
                .getBody();
    }

    // 提取 token 中的某些信息
    public <T> T extractClaim(String token, ClaimsResolver<T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.resolve(claims);
    }

    // 验证 Token 是否有效
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 获取 Token 的过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 验证 Token 是否有效（根据用户名和过期时间）
    public boolean validateToken(String token, String username) {
        return (username.equals(extractUsername(token)) && !isTokenExpired(token));
    }

    // Functional Interface to extract claims
    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}
