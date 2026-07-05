package com.ctg.dataFab.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT Token 提供者
 * 负责生成、验证 JWT 令牌
 *
 * @author Developer
 * @since 2026-07-03
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long jwtExpiration;
    private final long refreshExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long jwtExpiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpiration = jwtExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    /**
     * 生成访问令牌
     */
    public String generateToken(String username, List<String> roles) {
        return generateToken(username, null, roles);
    }

    /**
     * 生成访问令牌 (带userId)
     */
    public String generateToken(String username, Long userId, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        var builder = Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey);

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder.compact();
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /**
     * 从令牌中获取角色
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("roles", List.class);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT令牌无效: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT令牌验证失败", e);
        }
        return false;
    }

    /**
     * 获取令牌 Claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从令牌获取认证信息
     */
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        Long userId = getUserIdFromToken(token);
        List<String> roles = getRolesFromToken(token);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        // 将 userId 存入 details
        User principal = new User(username, "", authorities);
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, token, authorities);
    }
}
