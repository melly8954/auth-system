package com.authsystem.authjwt.auth.service;

import com.authsystem.authjwt.auth.info.GoogleUserInfo;
import com.authsystem.authjwt.auth.info.KakaoUserInfo;
import com.authsystem.authjwt.auth.info.OAuth2UserInfo;
import com.authsystem.authjwt.auth.security.principal.PrincipalDetails;
import com.authsystem.authjwt.user.entity.User;
import com.authsystem.authjwt.user.entity.UserProfile;
import com.authsystem.authjwt.user.entity.UserRole;
import com.authsystem.authjwt.user.entity.UserSocialAccount;
import com.authsystem.authjwt.user.entity.UserStatus;
import com.authsystem.authjwt.user.repository.UserProfileRepository;
import com.authsystem.authjwt.user.repository.UserRepository;
import com.authsystem.authjwt.user.repository.UserSocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrincipalOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = new OidcUserService().loadUser(userRequest); // ID Token 검증 포함
        OAuth2UserInfo userInfo;

        String provider = userRequest.getClientRegistration().getRegistrationId();

        if ("google".equals(provider)) {
            Map<String, Object> claims = oidcUser.getClaims(); // ID Token 클레임
            userInfo = new GoogleUserInfo(claims);
        } else if ("kakao".equals(provider)) {
            userInfo = new KakaoUserInfo(oidcUser.getAttributes()); // Kakao는 기존 구조
        } else {
            throw new IllegalArgumentException("지원하지 않는 provider: " + provider);
        }

        String providerId = userInfo.getProviderId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();
        String profileImage = userInfo.getProfileImage();

        // UserSocialAccount 기준으로 DB 조회
        UserSocialAccount social = userSocialAccountRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        User user;

        if (social != null) {
            // 기존 사용자 로그인
            user = social.getUser();
        } else {
            // 신규 사용자 가입
            user = User.builder()
                    .username(providerId)
                    .email(email)
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.USER)
                    .build();
            userRepository.save(user);

            UserProfile profile = UserProfile.builder()
                    .user(user)
                    .nickname(nickname)
                    .imageUrl(profileImage)
                    .build();
            userProfileRepository.save(profile);

            social = UserSocialAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerId(providerId)
                    .providerEmail(email)
                    .build();
            userSocialAccountRepository.save(social);
        }

        // PrincipalDetails 반환 (SecurityContext에 사용)
        return new PrincipalDetails(user, social, oidcUser);
    }
}
