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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

    /*
        Client
         ↓
        Security Filter Chain
         ↓
        JsonLoginFilter  // Security Filter가 직접 인증 처리
         ↓
        AuthenticationManager
         ↓
        AuthenticationProvider
         ↓
        UserDetailsService
         ↓
        SuccessHandler / FailureHandler
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/admins/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterAt(jsonLoginFilter(authenticationManager),
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
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JsonLoginFilter jsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonLoginFilter filter = new JsonLoginFilter(new ObjectMapper());
        /*
            인증 검증의 핵심 도구인 AuthenticationManager를 주입한다.
            실제 인증 로직은 AuthenticationManager 내부의 DaoAuthenticationProvider가 작동하여
            UserDetailsService로부터 읽어온 DB 유저 정보와 입력값을 대조한 후
            UsernamePasswordAuthenticationToken을 setAuthenticated(true) 설정한다.
         */
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 URL을 시큐리티 필터가 가로챕니다.

        // 인증 성공 시 SecurityContext를 HttpSession에 저장하도록 설정
        filter.setSecurityContextRepository(
                new HttpSessionSecurityContextRepository()
        );

        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        return filter;
    }

    /*
    // 직접 생성한 필터를 등록
            .addFilterAt(createJsonLoginFilter(authenticationManager),
                        UsernamePasswordAuthenticationFilter.class)
    // @Bean 어노테이션을 제거하고 일반 메서드로 변경
    private JsonLoginFilter createJsonLoginFilter(AuthenticationManager authenticationManager) {
        JsonLoginFilter filter = new JsonLoginFilter(objectMapper);
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/api/v1/auth/login");

        filter.setSecurityContextRepository(
                new HttpSessionSecurityContextRepository()
        );

        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        return filter;
    }
     */

    /*
        AuthenticationManager
            - 인증의 총괄 관리자인 AuthenticationManager를 직접 생성 및 등록한다.
            1. DaoAuthenticationProvider를 생성하고, 사용자 조회(UserDetailsService)와
               비밀번호 검증(PasswordEncoder) 전략을 설정한다.
            2. 이를 관리하는 ProviderManager를 반환하여, 서비스의 전체적인 인증 프로세스를 완성한다.
     */
//    @Bean
//    public AuthenticationManager authenticationManager(
//            PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
//        // 실제 인증 로직(DB 대조 + PW 비교)을 수행할 프로바이더 생성
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder);
//
//        // 프로바이더를 리스트에 담아 관리자(ProviderManager)에게 위임
//        return new ProviderManager(provider);
//    }

}
