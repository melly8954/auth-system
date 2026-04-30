package com.authsystem.authjwt.user.service;

import com.authsystem.authjwt.user.dto.ProfileImageUploadUrlRequest;
import com.authsystem.authjwt.user.dto.ProfileImageUploadUrlResponse;

public interface ProfileImageService {
    ProfileImageUploadUrlResponse createUploadUrl(ProfileImageUploadUrlRequest request);
    String normalizeProfileImageUrl(String imageUrl);
}
