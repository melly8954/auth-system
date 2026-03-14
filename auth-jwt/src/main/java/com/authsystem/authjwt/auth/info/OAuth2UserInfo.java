package com.authsystem.authjwt.auth.info;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImage();
}
