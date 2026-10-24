package com.mc.create.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：签发与校验登录令牌。
 * - HS256 对称签名，密钥来自 application.yml（固定，重启不失效）
 * - token 中只存 userId（subject），不含密码等敏感信息
 * - 过期时间由 yml 的 jwt.expire 控制
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire}") long expireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    /** 签发 token：subject = userId，带过期时间，HS256 签名 */
    public String createToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))            // 载荷：用户id
                .issuedAt(now)                              // 签发时间
                .expiration(expiry)                         // 过期时间
                .signWith(key)                              // HS256 签名
                .compact();
    }

    /**
     * 校验并解析 token。
     * 签名错误 / 已过期 / token 非法时返回 null（前端会被 401 拦截）。
     */
    public Long parseUserId(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            // 过期、签名不匹配、格式错误都算无效令牌
            return null;
        }
    }
}
