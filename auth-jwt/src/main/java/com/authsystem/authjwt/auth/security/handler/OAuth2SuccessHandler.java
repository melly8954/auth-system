package com.authsystem.authjwt.auth.security.handler;

import com.authsystem.authjwt.auth.dto.RefreshTokenDto;
import com.authsystem.authjwt.auth.security.jwt.JwtUtil;
import com.authsystem.authjwt.auth.security.principal.PrincipalDetails;
import com.authsystem.authjwt.common.util.CookieUtil;
import com.authsystem.authjwt.user.entity.User;
import com.authsystem.authjwt.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.accessExpiredMs}")
    private long accessExpiredMs;

    @Value("${jwt.refreshExpiredMs}")
    private long refreshExpiredMs;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        User user = principal.getUser();
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.createJwt("AccessToken", user.getUsername(), user.getRole().name(), accessExpiredMs);
        String refreshToken = jwtUtil.createJwt("RefreshToken", user.getUsername(), user.getRole().name(), refreshExpiredMs);

        String refreshJti = jwtUtil.getTokenId(refreshToken);

        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .tokenId(refreshJti)
                .username(user.getUsername())
                .role(user.getRole().name())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiredMs)))
                .build();

        redisTemplate.opsForValue().set(refreshJti, refreshTokenDto, Duration.ofMillis(refreshExpiredMs));

        // 쿠키 생성
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", refreshToken, 1);
        response.addCookie(refreshCookie);

        response.sendRedirect(frontendUrl + "/oauth/callback");
    }
}
