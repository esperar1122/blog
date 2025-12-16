package com.example.blog.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";

    private SecretKey getSigningKey() {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            // 在开发环境中提供一个安全的默认值，但记录警告
            log.warn("JWT密钥未配置！使用开发环境默认值。生产环境必须设置环境变量JWT_SECRET");
            // 生成一个临时的安全密钥（仅用于开发）
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("tokenType", "access");

        return createToken(claims, username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("tokenType", "refresh");

        String refreshToken = createToken(claims, username, refreshTokenExpiration);

        // 将刷新令牌存储到Redis中
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(key, refreshToken, refreshTokenExpiration, TimeUnit.MILLISECONDS);

        log.debug("生成刷新令牌 - username: {}, 过期时间: {} 分钟",
                username, refreshTokenExpiration / 60000);

        return refreshToken;
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.error("无效的JWT令牌: {}", e.getMessage());
            throw e;
        }
    }

    public String extractUsername(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long extractUserId(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return getClaimsFromToken(token).get("tokenType", String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    public boolean isTokenExpired(String token) {
        try {
            return getClaimsFromToken(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateAccessToken(String token) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }

            if (!isAccessToken(token)) {
                log.warn("令牌类型错误，期望access token");
                return false;
            }

            if (isTokenBlacklisted(token)) {
                log.warn("令牌在黑名单中");
                return false;
            }

            return true;
        } catch (JwtException e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token, String username) {
        try {
            if (isTokenExpired(token)) {
                return false;
            }

            if (!isRefreshToken(token)) {
                log.warn("令牌类型错误，期望refresh token");
                return false;
            }

            String tokenUsername = extractUsername(token);
            if (!username.equals(tokenUsername)) {
                log.warn("刷新令牌用户名不匹配");
                return false;
            }

            // 检查Redis中是否存在该刷新令牌
            String key = REFRESH_TOKEN_PREFIX + username;
            String storedToken = redisTemplate.opsForValue().get(key);
            if (!token.equals(storedToken)) {
                log.warn("刷新令牌无效或已撤销");
                return false;
            }

            return true;
        } catch (JwtException e) {
            log.error("刷新令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    public void blacklistToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long ttl = expiration.getTime() - System.currentTimeMillis();

            if (ttl > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
                log.debug("令牌已加入黑名单 - username: {}, ttl: {} 分钟",
                        claims.getSubject(), ttl / 60000);
            }
        } catch (JwtException e) {
            log.warn("无法将令牌加入黑名单: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void revokeRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
        log.debug("撤销用户刷新令牌 - username: {}", username);
    }

    public void revokeAllUserTokens(String username) {
        // 撤销刷新令牌
        revokeRefreshToken(username);

        // 这里可以扩展为撤销用户的所有访问令牌
        // 但由于访问令牌是无状态的，我们需要维护一个用户令牌列表
        log.info("撤销用户所有令牌 - username: {}", username);
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpiration / 60000;
    }

    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpiration / (24 * 60 * 60 * 1000);
    }
}