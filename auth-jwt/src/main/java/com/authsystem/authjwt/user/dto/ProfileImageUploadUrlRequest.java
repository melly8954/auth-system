package com.authsystem.authjwt.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileImageUploadUrlRequest {
    private String fileName;
    private String contentType;
    private Long fileSize;
}
