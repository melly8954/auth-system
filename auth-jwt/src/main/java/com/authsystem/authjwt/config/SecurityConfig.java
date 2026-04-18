package com.authsystem.authjwt.config;

import com.authsystem.authjwt.auth.security.filter.JsonLoginFilter;
import com.authsystem.authjwt.auth.security.handler.CustomAccessDeniedHandler;
import com.authsystem.authjwt.auth.security.handler.CustomAuthenticationEntryPoint;
import com.authsystem.authjwt.auth.security.handler.LoginFailureHandler;
import com.authsystem.authjwt.auth.security.handler.LoginSuccessHandler;
import com.authsystem.authjwt.auth.security.handler.OAuth2FailHandler;
import com.authsystem.authjwt.auth.security.handler.OAuth2SuccessHandler;
import com.authsystem.authjwt.auth.security.jwt.JwtFilter;
import com.authsystem.authjwt.auth.security.jwt.JwtUtil;
import com.authsystem.authjwt.auth.service.CustomAuthorizationRequestResolver;
import com.authsystem.authjwt.auth.service.PrincipalDetailsService;
import com.authsystem.authjwt.auth.service.PrincipalOAuth2UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtUtil jwtUtil;
    private final PrincipalDetailsService principalDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailHandler oAuth2FailHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /*
        [AuthenticationManager 주입 방식]
        - AuthenticationManager는 Spring Security 내부에서 AuthenticationConfiguration을 통해 "지연 생성"되는 객체이다.
        - Config 생성 시점에는 아직 Bean이 완전히 준비되지 않음
        - 따라서 생성자 주입이 아닌, Bean 메서드 파라미터 주입 방식 사용

        → SecurityFilterChain 생성 시점에 안전하게 주입받는다.
    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                // - JsonLoginFilter를 사용한 커스텀 로그인으로 대체
                .formLogin(AbstractHttpConfigurer::disable)
                /*
                    HTTP Basic 인증 비활성화
                    - Authorization: Basic 방식 제거
                    - JWT 기반 인증과 충돌 및 불필요
                 */
                .httpBasic(AbstractHttpConfigurer::disable)
                /*
                    CSRF(Cross-Site Request Forgery) 보호 비활성화
                    - CSRF는 "쿠키 기반 인증(세션)"에서 발생하는 공격을 방어하기 위한 것
                    - 현재는 JWT를 Authorization 헤더로 전달 → 브라우저가 자동 전송하지 않음
                    - 따라서 CSRF 공격 대상이 아니므로 비활성화
                    ※ 단, JWT를 쿠키에 저장하는 경우에는 다시 활성화 필요
                 */
                .csrf(AbstractHttpConfigurer::disable)
                /*
                    CORS 설정 활성화
                    - 프론트엔드와 백엔드가 서로 다른 Origin일 경우 요청 허용 필요
                    - 기본 설정을 사용하며, 필요 시 CorsConfigurationSource로 세부 설정 가능
                 */
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                /*
                    [JWT 인증 필터]
                    - 모든 요청에서 JWT 토큰을 검사하여 인증 처리
                    - Authorization Header의 토큰을 검증 후 SecurityContext에 인증 정보 설정

                    [필터 위치]
                    - UsernamePasswordAuthenticationFilter 이전에 실행됨
                    → 로그인 요청 이전에 JWT 기반 인증을 먼저 처리하기 위함

                    [동작 흐름]
                    요청 → JwtFilter → (토큰 검증) → 성공 시 SecurityContext에 인증 정보 설정
                    이후 필터들은 인증된 사용자로 인식
                    (※ 해당 인증 정보는 요청 동안만 유지되며, 요청 종료 시 사라진다.)
                */
                .addFilterBefore(
                        new JwtFilter(jwtUtil, principalDetailsService, redisTemplate),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(createJsonLoginFilter(authenticationManager),
                        UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(new CustomAuthorizationRequestResolver(clientRegistrationRepository)))
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(principalOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailHandler))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패(401) 처리
                        .accessDeniedHandler(customAccessDeniedHandler)); // 인가 실패(403) 처리
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        /*
            [Spring Security 자동 구성 방식]

            - AuthenticationConfiguration을 통해 이미 구성된 AuthenticationManager를 반환한다.
            - 내부적으로 DaoAuthenticationProvider, UserDetailsService, PasswordEncoder 등이
              자동으로 연결된 상태이다.

            [특징]
            - Spring Security가 전체 인증 구조를 자동으로 조립
            - 설정 기반으로 Provider가 구성됨
            - 최신 권장 방식 (Spring Security 5.7+)
            - 직접 Provider를 생성하지 않음

            → 즉, "이미 완성된 AuthenticationManager를 꺼내 쓰는 방식"
        */
        return configuration.getAuthenticationManager();
    }

    // 직접 생성한 필터를 등록
    private JsonLoginFilter createJsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonLoginFilter filter = new JsonLoginFilter(new ObjectMapper());

        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 URL을 시큐리티 필터가 가로챕니다.

        // 인증 성공/실패 후 처리 로직
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        return filter;
    }
}

