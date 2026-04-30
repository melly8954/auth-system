package com.authsystem.authjwt.user.service;

import com.authsystem.authjwt.common.exception.CustomException;
import com.authsystem.authjwt.common.exception.ErrorType;
import com.authsystem.authjwt.config.R2Properties;
import com.authsystem.authjwt.user.dto.ProfileImageUploadUrlRequest;
import com.authsystem.authjwt.user.dto.ProfileImageUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileImageServiceImpl implements ProfileImageService {
    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final ObjectProvider<S3Presigner> s3PresignerProvider;
    private final R2Properties r2Properties;

    @Override
    public ProfileImageUploadUrlResponse createUploadUrl(ProfileImageUploadUrlRequest request) {
        ensureR2Enabled();

        String contentType = normalizeContentType(request.getContentType());
        validateFileSize(request.getFileSize());

        String extension = CONTENT_TYPE_TO_EXTENSION.get(contentType);
        if (extension == null) {
            throw new CustomException(ErrorType.INVALID_PROFILE_IMAGE_TYPE);
        }

        String objectKey = buildObjectKey(extension);
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();

        S3Presigner presigner = s3PresignerProvider.getIfAvailable();
        if (presigner == null) {
            throw new CustomException(ErrorType.PROFILE_IMAGE_UPLOAD_DISABLED);
        }

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(r2Properties.presignedUrlExpiryMinutes()))
                        .putObjectRequest(putObjectRequest)
                        .build()
        );

        return ProfileImageUploadUrlResponse.builder()
                .objectKey(objectKey)
                .uploadUrl(presignedRequest.url().toString())
                .imageUrl(buildPublicImageUrl(objectKey))
                .maxFileSizeBytes(r2Properties.maxFileSizeBytes())
                .build();
    }

    @Override
    public String normalizeProfileImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }

        String normalizedImageUrl = imageUrl.trim();
        String publicBaseUrl = trimTrailingSlash(r2Properties.publicBaseUrl());
        String allowedPrefix = publicBaseUrl + "/" + trimSlashes(r2Properties.uploadPrefix()) + "/";

        if (!normalizedImageUrl.startsWith(allowedPrefix)) {
            throw new CustomException(ErrorType.INVALID_PROFILE_IMAGE_URL);
        }

        return normalizedImageUrl;
    }

    private void ensureR2Enabled() {
        if (!r2Properties.enabled()) {
            throw new CustomException(ErrorType.PROFILE_IMAGE_UPLOAD_DISABLED);
        }
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw new CustomException(ErrorType.INVALID_PROFILE_IMAGE_TYPE);
        }

        String normalizedContentType = contentType.trim().toLowerCase(Locale.ROOT);
        if (!r2Properties.allowedContentTypes().contains(normalizedContentType)) {
            throw new CustomException(ErrorType.INVALID_PROFILE_IMAGE_TYPE);
        }

        return normalizedContentType;
    }

    private void validateFileSize(Long fileSize) {
        if (fileSize == null || fileSize <= 0 || fileSize > r2Properties.maxFileSizeBytes()) {
            throw new CustomException(ErrorType.INVALID_PROFILE_IMAGE_SIZE);
        }
    }

    private String buildObjectKey(String extension) {
        return trimSlashes(r2Properties.uploadPrefix()) + "/" + UUID.randomUUID() + extension;
    }

    private String buildPublicImageUrl(String objectKey) {
        return trimTrailingSlash(r2Properties.publicBaseUrl()) + "/" + objectKey;
    }

    private String trimSlashes(String value) {
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String trimTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
