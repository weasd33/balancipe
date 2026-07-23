package com.beokay.balancipe.global.security;

import com.beokay.balancipe.global.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    // 새 로그인 시 기존 값을 덮어써 사용자당 하나의 Refresh Token(단일 세션)만 유지한다.
    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
            key(userId),
            refreshToken,
            Duration.ofMillis(jwtProperties.refreshTokenExpiration())
        );
    }

    public Optional<String> findByUserId(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
    }

    public void deleteByUserId(Long userId) {
        redisTemplate.delete(key(userId));
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
