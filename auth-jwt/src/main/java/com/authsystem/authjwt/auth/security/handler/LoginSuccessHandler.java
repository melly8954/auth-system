package com.authsystem.authjwt.auth.security.handler;

import com.authsystem.authjwt.auth.dto.LoginResponse;
import com.authsystem.authjwt.auth.dto.RefreshTokenDto;
import com.authsystem.authjwt.auth.repository.AuthTokenRedisRepository;
import com.authsystem.authjwt.auth.security.jwt.JwtUtil;
import com.authsystem.authjwt.auth.security.principal.PrincipalDetails;
import com.authsystem.authjwt.common.domain.dto.ApiResponse;
import com.authsystem.authjwt.common.util.CookieUtil;
import com.authsystem.authjwt.user.entity.User;
import com.authsystem.authjwt.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final AuthTokenRedisRepository authTokenRedisRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        User user = principal.getUser();
        user.updateLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtUtil.createJwt("AccessToken", user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.createJwt("RefreshToken", user.getUsername(), user.getRole().name());

        String refreshJti = jwtUtil.getTokenId(refreshToken);

        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .tokenId(refreshJti)
                .username(user.getUsername())
                .role(user.getRole().name())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshTokenExpiredMs())))
                .build();

        authTokenRedisRepository.saveRefreshToken(refreshJti, refreshTokenDto, jwtUtil.getRefreshTokenExpiredMs());

        // 쿠키 생성
        Cookie refreshCookie = cookieUtil.createCookie("RefreshToken", refreshToken, 1);
        response.addCookie(refreshCookie);

        LoginResponse dto = LoginResponse.builder()
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .build();

        ApiResponse<?> apiResponse = ApiResponse.handlerOf(HttpStatus.OK, null, "로그인 성공", dto);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
