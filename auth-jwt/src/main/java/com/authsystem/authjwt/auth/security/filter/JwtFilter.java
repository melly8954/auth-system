package com.authsystem.authjwt.auth.security.filter;

import com.authsystem.authjwt.auth.repository.AuthTokenRedisRepository;
import com.authsystem.authjwt.auth.security.jwt.JwtUtil;
import com.authsystem.authjwt.auth.security.principal.PrincipalDetails;
import com.authsystem.authjwt.auth.service.PrincipalDetailsService;
import com.authsystem.authjwt.common.domain.dto.ApiResponse;
import com.authsystem.authjwt.common.exception.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;


/*
    [JWT 인증 필터]

    - 모든 요청마다 실행되며, JWT 토큰 기반 인증을 처리한다.
    - 세션을 사용하지 않는 Stateless 환경에서 인증을 대신 수행하는 핵심 컴포넌트

    [핵심 역할]
    1. Authorization Header에서 JWT 추출
    2. 토큰 유효성 검증 (만료, 타입, 블랙리스트 등)
    3. 사용자 정보 조회 (UserDetailsService)
    4. Authentication 객체 생성
    5. SecurityContext에 인증 정보 저장

    → 이후 필터 및 컨트롤러는 인증된 사용자로 인식
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    // OncePerRequestFilter 는 Spring Framework 에서 제공하는 추상 클래스로, 하나의 HTTP 요청(request)당 딱 한 번만 실행되는 필터
    // JWT 검증 로직은 인증이 필요한 요청이 들어올 때마다 확실히 한 번만 실행되도록 보장
    private final JwtUtil jwtUtil;
    private final PrincipalDetailsService principalDetailsService;
    private final AuthTokenRedisRepository authTokenRedisRepository;

    public JwtFilter(JwtUtil jwtUtil, PrincipalDetailsService principalDetailsService, AuthTokenRedisRepository authTokenRedisRepository) {
        this.jwtUtil = jwtUtil;
        this.principalDetailsService = principalDetailsService;
        this.authTokenRedisRepository = authTokenRedisRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 헤더에서 access 키에 담긴 토큰을 꺼냄
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            // JWT가 없는 요청 → 인증 대상이 아니므로 그대로 다음 필터로 전달
            filterChain.doFilter(request, response);

            // 이후 JWT 검증 로직을 수행하지 않고 현재 필터 종료
            return;
        }

        String accessToken = header.substring(7); // "Bearer " 이후의 토큰만 추출

        // 토큰 만료 여부 확인
        if (jwtUtil.isExpired(accessToken)) {
            log.error("Expired JWT token");

            sendErrorResponse(
                    response,
                    ErrorType.ACCESS_TOKEN_EXPIRED.getHttpStatus().value(),
                    ErrorType.ACCESS_TOKEN_EXPIRED.getErrorCode(),
                    ErrorType.ACCESS_TOKEN_EXPIRED.getMessage()
            );

            // 유효하지 않은 토큰 → 인증 실패 처리 후 요청 종료 (다음 필터로 전달되지 않음)
            return;
        }

        // 토큰의 종류가 access 인지 검증 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("AccessToken")) {
            log.error("Invalid JWT token");

            sendErrorResponse(
                    response,
                    ErrorType.ACCESS_TOKEN_INVALID.getHttpStatus().value(),
                    ErrorType.ACCESS_TOKEN_INVALID.getErrorCode(),
                    ErrorType.ACCESS_TOKEN_INVALID.getMessage()
            );

            // AccessToken이 아닌 경우 → 인증 실패로 간주하고 요청 종료
            return;
        }

        // 블랙리스트 체크
        if (authTokenRedisRepository.isBlacklistedAccessToken(accessToken)) {
            log.error("Blacklisted JWT token");

            sendErrorResponse(
                    response,
                    ErrorType.ACCESS_TOKEN_OF_BLACK_LIST.getHttpStatus().value(),
                    ErrorType.ACCESS_TOKEN_OF_BLACK_LIST.getErrorCode(),
                    ErrorType.ACCESS_TOKEN_OF_BLACK_LIST.getMessage()
            );
            // 로그아웃/강제 만료된 토큰 → 인증 불가, 요청 즉시 종료
            return;
        }

        // username 값 획득
        String username = jwtUtil.getUsername(accessToken);

        PrincipalDetails principalDetails = (PrincipalDetails) principalDetailsService.loadUserByUsername(username);
        Authentication authToken = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

        /*
            SecurityContext에 인증 정보 저장
            - 해당 요청 동안 인증 상태 유지
            - 요청 종료 시 SecurityContext는 사라짐 (Stateless)
            → 이후 필터 및 Controller에서 인증된 사용자로 인식
        */
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 다음 필터 진행 filterChain.doFilter()
        filterChain.doFilter(request, response);
    }


    /*
       [JWT 필터 내 예외 처리]

       - Filter는 DispatcherServlet 이전 단계에서 동작
       - 즉, @ControllerAdvice (@ExceptionHandler) 적용되지 않음
       - 따라서 직접 JSON 응답 작성 필요
    */
    private void sendErrorResponse(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        ApiResponse<Object> responseDto = ApiResponse.builder()
                .code(status)
                .errorCode(errorCode)
                .message(message)
                .result(null)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(responseDto);

        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }
}
