package com.authsystem.authjwt.auth.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String message;
        String errorCode;
        log.info("OAuth2FailHandler called");
        if (exception instanceof LockedException) {
            message = "계정이 일시정지 되었습니다.";
            errorCode = "AUTH_002";
        } else {
            message = "소셜 로그인에 실패했습니다.";
            errorCode = "AUTH_001";
        }

        String redirectUrl = frontendUrl +
                "/oauth/callback?errorCode=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8) +
                "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        log.info(redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
