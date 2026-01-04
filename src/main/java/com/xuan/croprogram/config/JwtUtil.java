package com.xuan.croprogram.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 1. 定义一个固定的、足够长的字符串（至少32位，像你的代码一样严谨）
    private final String SECRET_STRING = "ArchitectXuan_Secret_Key_For_Kingdom_2025_Safe";

    // 2. 将它转换成 SecretKey 对象
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    // JwtUtil.java

    // 之前的：public String generateToken(String phoneNumber)
// 升级版：
    public String generateToken(Long id,String phoneNumber, Long roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("phoneNumber", phoneNumber);
        claims.put("roleId", roleId); // ✅ 把职位刻在工牌上

        return Jwts.builder()
                .setClaims(claims) // 把这些信息都塞进去
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24小时有效期
                .signWith(SignatureAlgorithm.HS256, secretKey) // 你的秘钥
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
    // 在 JwtUtil 类中增加
    public Long getUserIdFromToken(String token) {
        // 1. 拿到所有信息
        Claims claims = extractAllClaims(token);

        // 2. 从 claims 中取出 id (当初 generateToken 存的时候 key 是 "id")
        Object id = claims.get("id");

        // 3. 转成 Long 返回
        if (id != null) {
            // 这里的 toString() 再 Long.valueOf 是最稳的，防止 Json 解析时的类型转换坑
            return Long.valueOf(id.toString());
        }
        return null;
    }
    // 在 JwtUtil 类中增加这个方法
    public Long getRoleIdFromToken(String token) {
        // 1. 调用你写好的解析方法拿到所有的 Claims
        Claims claims = extractAllClaims(token);

        // 2. 从 claims 中取出 roleId
        // 因为你存进去的时候是 Long，这里取出来时强转或者指定类型即可
        Object roleId = claims.get("roleId");
        if (roleId != null) {
            return Long.valueOf(roleId.toString());
        }
        return null;
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
