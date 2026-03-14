package com.authsystem.authjwt.auth.service;

import com.authsystem.authjwt.auth.dto.ReIssueTokenDto;
import com.authsystem.authjwt.auth.dto.RefreshTokenDto;
import com.authsystem.authjwt.auth.security.jwt.JwtUtil;
import com.authsystem.authjwt.common.exception.CustomException;
import com.authsystem.authjwt.common.exception.ErrorType;
import com.authsystem.authjwt.common.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.jwt.accessExpiredMs}")
    private long accessExpiredMs;

    @Value("${spring.jwt.refreshExpiredMs}")
    private long refreshExpiredMs;

    @Override
    public ReIssueTokenDto reissueToken(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키에서 refresh 토큰 추출
        String refreshToken = cookieUtil.getValue(request, "RefreshToken");
        if (refreshToken == null) {
            throw new CustomException(ErrorType.REFRESH_TOKEN_NOT_FOUND);
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(refreshToken)) {
            throw new CustomException(ErrorType.REFRESH_TOKEN_EXPIRED);
        }

        // 카테고리 확인
        if (!"RefreshToken".equals(jwtUtil.getCategory(refreshToken))) {
            throw new CustomException(ErrorType.REFRESH_TOKEN_INVALID);
        }

        String username = jwtUtil.getUsername(refreshToken);
        String tokenId = jwtUtil.getTokenId(refreshToken);

        String key = "RefreshToken:" + username + ":" + tokenId;
        Object redisValue = redisTemplate.opsForValue().get(key);

        if (redisValue == null) {
            throw new CustomException(ErrorType.REFRESH_TOKEN_NOT_IN_REDIS);
        }

        // Object -> DTO 변환
        RefreshTokenDto refreshTokenDto = objectMapper.convertValue(redisValue, RefreshTokenDto.class);

        // 새로운 tokenId 생성
        String newTokenId = UUID.randomUUID().toString();

        // 새로운 accessToken, refreshToken 생성
        String newAccessToken = jwtUtil.createJwt("AccessToken", username, refreshTokenDto.getRole(), newTokenId, accessExpiredMs);
        String newRefreshToken = jwtUtil.createJwt("RefreshToken", username, refreshTokenDto.getRole(), newTokenId, refreshExpiredMs);

        // Redis에 새로운 refreshToken 저장
        RefreshTokenDto newRefreshTokenDto = new RefreshTokenDto(
                newTokenId, username, refreshTokenDto.getRole(),
                LocalDateTime.now(),
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpiredMs))
        );
        redisTemplate.opsForValue().set("RefreshToken:" + username + ":" + newTokenId, newRefreshTokenDto, Duration.ofDays(1));

        // 기존 refresh token 삭제
        redisTemplate.delete(key);

        // 쿠키에 새로운 refreshToken 저장
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", newRefreshToken, 1);
        response.addCookie(refreshCookie);

        return new ReIssueTokenDto(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = request.getHeader("Authorization");

        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7); // "Bearer " 제거
        }

        // 토큰에서 남은 만료 시간 계산
        long expiration = jwtUtil.getExpiration(accessToken);

        // Redis 블랙리스트에 저장 (TTL 설정)
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "BLACKLIST_" + accessToken,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }

        String refreshToken = cookieUtil.getValue(request, "RefreshToken");

        String username = jwtUtil.getUsername(refreshToken);
        String tokenId = jwtUtil.getTokenId(refreshToken);

        String key = "RefreshToken:" + username + ":" + tokenId;

        redisTemplate.delete(key);

        // 쿠키에서 refresh token 제거
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", null, 0);

        response.addCookie(refreshCookie);
    }
}
