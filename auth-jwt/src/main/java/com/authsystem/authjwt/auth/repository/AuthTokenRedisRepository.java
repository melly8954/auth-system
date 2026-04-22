package com.authsystem.authjwt.auth.repository;

import com.authsystem.authjwt.auth.dto.RefreshTokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class AuthTokenRedisRepository {
    private static final String BLACKLIST_PREFIX = "BLACKLIST_";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveRefreshToken(String tokenId, RefreshTokenDto refreshTokenDto, long ttlMs) {
        redisTemplate.opsForValue().set(tokenId, refreshTokenDto, Duration.ofMillis(ttlMs));
    }

    public void saveBlacklist(String accessToken, long ttlMs) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                "logout",
                ttlMs,
                TimeUnit.MILLISECONDS
        );
    }

    public RefreshTokenDto findRefreshToken(String tokenId) {
        Object redisValue = redisTemplate.opsForValue().get(tokenId);
        if (redisValue == null) {
            return null;
        }

        return objectMapper.convertValue(redisValue, RefreshTokenDto.class);
    }

    public void deleteRefreshToken(String tokenId) {
        redisTemplate.delete(tokenId);
    }

    public boolean hasBlacklistedToken(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken));
    }
}
