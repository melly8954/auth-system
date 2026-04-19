package com.authsystem.authsession.config;

import com.authsystem.authsession.auth.security.filter.JsonLoginFilter;
import com.authsystem.authsession.auth.security.handler.CustomAccessDeniedHandler;
import com.authsystem.authsession.auth.security.handler.CustomAuthenticationEntryPoint;
import com.authsystem.authsession.auth.security.handler.LoginFailureHandler;
import com.authsystem.authsession.auth.security.handler.LoginSuccessHandler;
import com.authsystem.authsession.auth.security.handler.OAuth2FailHandler;
import com.authsystem.authsession.auth.security.handler.OAuth2SuccessHandler;
import com.authsystem.authsession.auth.service.CustomAuthorizationRequestResolver;
import com.authsystem.authsession.auth.service.PrincipalOAuth2UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityConfig.CorsProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailHandler oAuth2FailHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CorsProperties corsProperties;

    /*
        Client → JsonLoginFilter → AuthenticationManager → AuthenticationProvider
           → UserDetailsService → 인증 성공/실패 Handler
    */

    /*
        [AuthenticationManager 주입 방식]
        - AuthenticationManager는 Spring Security 내부에서 AuthenticationConfiguration을 통해 "지연 생성"되는 객체이다.
        - Config 생성 시점에는 아직 Bean이 완전히 준비되지 않음
        - 따라서 생성자 주입이 아닌, Bean 메서드 파라미터 주입 방식 사용

        → SecurityFilterChain 생성 시점에 안전하게 주입받는다.
    */
    @Bean                                                                 // 지연 생성 객체이므로 파라미터 주입 사용
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // - JsonLoginFilter를 사용한 커스텀 로그인으로 대체
                .formLogin(AbstractHttpConfigurer::disable)
                /*
                    HTTP Basic 인증 비활성화
                    - Authorization: Basic 방식 제거
                    - JWT 기반 인증과 충돌 및 불필요 (세션 기반에서는 선택 → 굳이 활성화 할 이유없음)
                 */
                .httpBasic(AbstractHttpConfigurer::disable)
                /*
                    CSRF(Cross-Site Request Forgery) 보호 비활성화
                    - CSRF는 "쿠키 기반 인증(세션)"에서 발생하는 공격을 방어하기 위한 것
                    - 세션 기반 인증은 쿠키를 사용 → 브라우저가 자동으로 쿠키 전송
                    - 이를 이용한 CSRF 공격 가능
                    - 따라서 반드시 활성화하여 토큰 검증 수행
                    - X-CSRF-TOKEN: xxx 처럼 프론트에서 토큰을 같이 보내야 함
                 */
                .csrf(AbstractHttpConfigurer::disable)  // 테스트를 위해 일단 disable
                /*
                    CORS 설정 활성화
                    - 프론트엔드와 백엔드가 서로 다른 Origin일 경우 요청 허용 필요
                    - 기본 설정을 사용하며, 필요 시 CorsConfigurationSource로 세부 설정 가능
                 */
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterAt(createJsonLoginFilter(authenticationManager),
                        UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(
                                        new CustomAuthorizationRequestResolver(clientRegistrationRepository)
                                ))
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(principalOAuth2UserService))
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
    AuthenticationManager
        - 인증의 총괄 관리자인 AuthenticationManager를 직접 생성 및 등록한다.
        1. DaoAuthenticationProvider를 생성하고, 사용자 조회(UserDetailsService)와
           비밀번호 검증(PasswordEncoder) 전략을 설정한다.
        2. 이를 관리하는 ProviderManager를 반환하여, 서비스의 전체적인 인증 프로세스를 완성한다.

        [수동 구성 방식]

        - 개발자가 AuthenticationProvider를 직접 생성하여 AuthenticationManager를 구성한다.
        - 인증 로직(DB 조회, 비밀번호 검증 등)을 수행할 Provider를 명시적으로 설정한다.

        [특징]
        - 인증 구조를 세밀하게 커스터마이징 가능
        - Provider를 여러 개 등록 가능 (ex: DB + OAuth + LDAP)
        - 대신 설정 누락 시 인증 실패 가능성 존재

        → 즉, "AuthenticationManager를 직접 조립하는 방식"
    @Bean
    public AuthenticationManager authenticationManager(
            PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        // 실제 인증 로직(DB 대조 + PW 비교)을 수행할 프로바이더 생성
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        // 프로바이더를 리스트에 담아 관리자(ProviderManager)에게 위임
        return new ProviderManager(provider);
    }
    */

    // 직접 생성한 필터를 등록
    private JsonLoginFilter createJsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonLoginFilter filter = new JsonLoginFilter(new ObjectMapper());
        /*
            인증 검증의 핵심 도구인 AuthenticationManager를 주입한다.
            실제 인증 로직은 AuthenticationManager 내부의 DaoAuthenticationProvider가 작동하여
            UserDetailsService로부터 읽어온 DB 유저 정보와 입력값을 대조한 후
            UsernamePasswordAuthenticationToken을 setAuthenticated(true) 설정한다.
         */
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 URL을 시큐리티 필터가 가로챕니다.

        /*
            인증 성공 시 SecurityContext를 세션에 저장 → 로그인 상태 유지
        */
        filter.setSecurityContextRepository(
                new HttpSessionSecurityContextRepository()
        );

        // 인증 성공/실패 후 처리 로직
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        return filter;
    }

    @ConfigurationProperties(prefix = "app.cors")
    public record CorsProperties(List<String> allowedOrigins) {
    }
}
