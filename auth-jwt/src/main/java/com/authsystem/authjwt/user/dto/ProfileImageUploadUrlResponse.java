package com.authsystem.authjwt.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileImageUploadUrlResponse {
    private String objectKey;
    private String uploadUrl;
    private String imageUrl;
    private long maxFileSizeBytes;
}
