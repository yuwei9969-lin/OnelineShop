package com.comp5348.storeapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    // 从 application.properties 中读取 jwt.secret 和 jwt.expiration
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    // 生成 JWT Token
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    // 从 Token 中提取用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 从 Token 中提取过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 通用方法：提取 Token 中的任意声明
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    // 检查 Token 是否过期
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
