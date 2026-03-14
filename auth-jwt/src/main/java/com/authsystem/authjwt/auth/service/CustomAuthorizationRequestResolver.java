package com.authsystem.authjwt.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

import java.util.HashMap;
import java.util.Map;

public class CustomAuthorizationRequestResolver
        implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(
            ClientRegistrationRepository repo) {

        this.defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        repo,
                        "/oauth2/authorization"
                );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest =
                defaultResolver.resolve(request);

        if (authorizationRequest == null) {
            return null;
        }

        String registrationId =
                (String) authorizationRequest.getAttributes()
                        .get(OAuth2ParameterNames.REGISTRATION_ID);

        Map<String, Object> additionalParameters =
                new HashMap<>(authorizationRequest.getAdditionalParameters());

        // none VS prompt
        if ("google".equals(registrationId)) {
            additionalParameters.put("prompt", "select_account");
        }

        if ("kakao".equals(registrationId)) {
            additionalParameters.put("prompt", "select_account");
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }

    @Override
    public OAuth2AuthorizationRequest resolve(
            HttpServletRequest request, String clientRegistrationId) {
        return resolve(request);
    }
}