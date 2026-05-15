package com.nlizzard.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // 生产环境请替换为安全的密钥，长度至少为 32 字节
    private static final String SECRET_KEY_STRING = "Z2hKNGw1bTZuN284cDlxMHJyN3M4dDl1M3Y0dzV4Nnk=";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());

    /**
     * 生成包含 userId 且永不过期的 Token
     */
    public static String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId) // 将 userId 存入 subject
                .setIssuedAt(new Date()) // 签发时间（可选，建议保留以区分不同时间生成的 token）
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact(); // 去掉了 setExpiration()
    }

    /**
     * 从 Token 中解析出 userId
     * @return userId，若 Token 被篡改或非法则返回 null
     */
    public static String getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            // 签名不匹配或 token 被篡改
            return null;
        }
    }
}

