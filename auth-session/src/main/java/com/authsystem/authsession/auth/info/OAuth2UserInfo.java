package com.authsystem.authsession.auth.info;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImage();
}
