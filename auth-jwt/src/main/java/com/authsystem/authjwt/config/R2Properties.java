package com.authsystem.authjwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.r2")
public record R2Properties(
        boolean enabled,
        String accountId,
        String accessKeyId,
        String secretAccessKey,
        String bucket,
        String publicBaseUrl,
        String uploadPrefix,
        int presignedUrlExpiryMinutes,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
    public String endpoint() {
        return "https://" + accountId + ".r2.cloudflarestorage.com";
    }
}
