package com.authsystem.authsession.config;

import com.authsystem.authsession.auth.security.filter.JsonLoginFilter;
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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    private final PrincipalOAuth2UserService principalOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailHandler oAuth2FailHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
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
                // .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
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
                        .failureHandler(oAuth2FailHandler));
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
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/api/v1/auth/login"); // 로그인 URL을 시큐리티 필터가 가로챕니다.

        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);

        return filter;
    }
}
