package com.seckill.mall.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类——生成和解析 Token。
 * 只负责 Token 本身，不负责存 Redis。存取逻辑在 Service 层。
 */
public class JwtUtil {

    private static final String SECRET = "seckill-mall-jwt-secret-key-2026-very-long!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // Access Token 30分钟，Refresh Token 7天
    private static final long ACCESS_EXPIRE = 30 * 60 * 1000L;
    private static final long REFRESH_EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 Access Token
     */
    public static String generateAccessToken(Long userId) {
        return generateToken(userId, ACCESS_EXPIRE);
    }

    /**
     * 生成 Refresh Token
     */
    public static String generateRefreshToken(Long userId) {
        return generateToken(userId, REFRESH_EXPIRE);
    }

    private static String generateToken(Long userId, long expireMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMs))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析 Token 中的 userId
     */
    public static Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    /**
     * 校验 Token 是否有效
     */
    public static boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
