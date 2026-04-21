package com.authsystem.authjwt.auth.service;

import com.authsystem.authjwt.auth.dto.ReIssueTokenDto;
import com.authsystem.authjwt.auth.dto.RefreshTokenDto;
import com.authsystem.authjwt.auth.security.jwt.JwtUtil;
import com.authsystem.authjwt.common.exception.CustomException;
import com.authsystem.authjwt.common.exception.ErrorType;
import com.authsystem.authjwt.common.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public ReIssueTokenDto reissueToken(String refreshToken, HttpServletResponse response) {
        // RefreshToken 존재하지 않는 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorType.REFRESH_TOKEN_NOT_FOUND);
        }

        // 토큰 만료 확인
        if (jwtUtil.isExpired(refreshToken)) {
            response.addCookie(cookieUtil.deleteCookie("RefreshToken"));
            throw new CustomException(ErrorType.REFRESH_TOKEN_EXPIRED);
        }

        // 카테고리 확인
        if (!"RefreshToken".equals(jwtUtil.getCategory(refreshToken))) {
            response.addCookie(cookieUtil.deleteCookie("RefreshToken"));
            throw new CustomException(ErrorType.REFRESH_TOKEN_INVALID);
        }

        String username = jwtUtil.getUsername(refreshToken);
        String redisKey = jwtUtil.getTokenId(refreshToken);
        Object redisValue = redisTemplate.opsForValue().get(redisKey);

        if (redisValue == null) {
            response.addCookie(cookieUtil.deleteCookie("RefreshToken"));
            throw new CustomException(ErrorType.REFRESH_TOKEN_NOT_IN_REDIS);
        }

        // Object -> DTO 변환
        RefreshTokenDto refreshTokenDto = objectMapper.convertValue(redisValue, RefreshTokenDto.class);

        // 새로운 accessToken, refreshToken 생성
        String newAccessToken = jwtUtil.createJwt("AccessToken", username, refreshTokenDto.getRole());
        String newRefreshToken = jwtUtil.createJwt("RefreshToken", username, refreshTokenDto.getRole());

        // 새로운 jti
        String newRefreshJti = jwtUtil.getTokenId(newRefreshToken);

        // Redis에 새로운 refreshToken 저장
        RefreshTokenDto newRefreshTokenDto = RefreshTokenDto.builder()
                .tokenId(newRefreshJti)
                .username(username)
                .role(refreshTokenDto.getRole())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshExpiredMs())))
                .build();

        redisTemplate.opsForValue().set(newRefreshJti, newRefreshTokenDto, Duration.ofMillis(jwtUtil.getRefreshExpiredMs()));

        // 기존 refresh token 삭제
        redisTemplate.delete(redisKey);

        // 쿠키에 새로운 refreshToken 저장
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", newRefreshToken, 1);
        response.addCookie(refreshCookie);

        return ReIssueTokenDto.builder().newAccessToken(newAccessToken).build();
    }

    @Override
    public void logout(String bearerToken, String refreshToken, HttpServletResponse response) {
        String accessToken = null;

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            accessToken = bearerToken.substring(7); // "Bearer " 제거
        }

        // 토큰에서 남은 만료 시간 계산
        long expiration = jwtUtil.getRemainingExpirationMillis(accessToken);

        // Redis 블랙리스트에 저장 (TTL 설정)
        if (expiration > 0) {
            redisTemplate.opsForValue().set(
                    "BLACKLIST_" + accessToken,
                    "logout",
                    expiration,
                    TimeUnit.MILLISECONDS
            );
        }

        String key = jwtUtil.getTokenId(refreshToken);

        redisTemplate.delete(key);

        // 쿠키에서 refresh token 제거
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", null, 0);

        response.addCookie(refreshCookie);
    }
}
