package com.authsystem.authsession.auth.security.handler;

import com.authsystem.authsession.auth.security.principal.PrincipalDetails;
import com.authsystem.authsession.common.domain.dto.ApiResponse;
import com.authsystem.authsession.user.entity.User;
import com.authsystem.authsession.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        PrincipalDetails principal =
                (PrincipalDetails) authentication.getPrincipal();

        User user = principal.getUser();
        user.updateLastLoginAt(LocalDateTime.now());

        userRepository.save(user);

        ApiResponse<?> apiResponse = ApiResponse.handlerOf(HttpStatus.OK, null, "로그인 성공", null);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
