package com.authsystem.authsession.auth.security.filter;

import com.authsystem.authsession.auth.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JsonLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final ObjectMapper objectMapper;

    // 여기서 getAuthenticationManager()를 호출
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 요청을 LoginRequest 객체로 변환
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            // 인증 요청용 토큰 생성 (아직 인증되지 않은 상태)
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            /*
                AuthenticationManager에 인증 위임

                내부 동작:
                → DaoAuthenticationProvider 호출 (Spring Security 기본 제공, supports()를 통해서 결정)
                → UserDetailsService로 사용자 조회
                → PasswordEncoder로 비밀번호 검증
                → 인증 성공 시 setAuthenticated(true) 토큰 반환
            */
            return this.getAuthenticationManager().authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
