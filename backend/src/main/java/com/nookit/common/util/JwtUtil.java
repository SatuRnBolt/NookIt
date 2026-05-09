package com.nookit.common.util;

import com.nookit.common.constant.SecurityConstants;
import com.nookit.common.exception.AuthException;
import com.nookit.common.api.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类。负责签发、解析与校验。
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "nookit.jwt")
public class JwtUtil {

    private String secret;
    private String issuer = "nookit";
    private long expireMinutes = 1440;
    private long refreshExpireMinutes = 10080;
    private String header = SecurityConstants.AUTH_HEADER;
    private String tokenPrefix = SecurityConstants.TOKEN_PREFIX;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("nookit.jwt.secret 必须配置且不少于 32 字节");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, Map<String, Object> extraClaims) {
        return buildToken(userId, username, extraClaims, Duration.ofMinutes(expireMinutes));
    }

    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, Map.of("type", "refresh"), Duration.ofMinutes(refreshExpireMinutes));
    }

    private String buildToken(Long userId, String username, Map<String, Object> extraClaims, Duration ttl) {
        Date now = new Date();
        Map<String, Object> claims = new HashMap<>();
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }
        claims.put(SecurityConstants.CLAIM_USER_ID, userId);
        claims.put(SecurityConstants.CLAIM_USERNAME, username);
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AuthException(ResultCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("invalid jwt: {}", e.getMessage());
            throw new AuthException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 从请求头原始值中剥离 {@code Bearer } 前缀。
     */
    public String resolveBearer(String rawHeader) {
        if (rawHeader == null || !rawHeader.startsWith(tokenPrefix)) {
            return null;
        }
        return rawHeader.substring(tokenPrefix.length()).trim();
    }
}
