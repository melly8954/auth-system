package com.authsystem.authjwt.auth.security.principal;

import com.authsystem.authjwt.user.entity.User;
import com.authsystem.authjwt.user.entity.UserSocialAccount;
import com.authsystem.authjwt.user.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class PrincipalDetails implements UserDetails, OidcUser {
    private final User user;
    private final UserSocialAccount userSocialAccount;
    private final OidcUser oidcUser;

    public PrincipalDetails(User user) {
        this.user = user;
        this.userSocialAccount = null;
        this.oidcUser = null;
    }

    public PrincipalDetails(User user,
                            UserSocialAccount userSocialAccount,
                            OidcUser oidcUser) {
        this.user = user;
        this.userSocialAccount = userSocialAccount;
        this.oidcUser = oidcUser;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser != null ? oidcUser.getAttributes() : Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*
            GrantedAuthority 인터페이스를 직접 구현해서 문자열 반환
            // Collection<GrantedAuthority> collection = new ArrayList<>();
            // collection.add((GrantedAuthority) () -> "ROLE_" + user.getRole().name());
        */

        // Spring에서 제공하는 표준 구현체 사용
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return oidcUser != null ? oidcUser.getName() : null;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }

    // 계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.INACTIVE;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser != null ? oidcUser.getClaims() : Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser != null ? oidcUser.getUserInfo() : null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser != null ? oidcUser.getIdToken() : null;
    }

    @Override
    public String getName() {
        return oidcUser != null ? oidcUser.getName() : user.getUsername();
    }

    // @Getter 사용하여
    // SecurityContext에서 PrincipalDetails를 꺼내서 내부 User 객체에 접근한다.
    // public User getUser() {
    //     return user;
    // }
}
