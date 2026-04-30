package com.authsystem.authjwt.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    // user
    USER_NOT_FOUND("USER_001", "해당 유저가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_USERNAME("USER_002", "이미 존재하는 아이디입니다.", HttpStatus.CONFLICT),
    DUPLICATE_EMAIL("USER_003", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
    PROFILE_IMAGE_UPLOAD_DISABLED("USER_004", "프로필 이미지 업로드가 비활성화되어 있습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_PROFILE_IMAGE_TYPE("USER_005", "jpg, png, webp 이미지 파일만 업로드할 수 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PROFILE_IMAGE_SIZE("USER_006", "프로필 이미지는 허용된 최대 크기 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_PROFILE_IMAGE_URL("USER_007", "허용되지 않은 프로필 이미지 URL입니다.", HttpStatus.BAD_REQUEST),

    // auth
    ACCESS_TOKEN_NOT_FOUND("auth_access_001", "Access Token을 요청받지 못했습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED("auth_access_002", "만료된 Access Token입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_INVALID("auth_access_003", "유효하지 않은 Access Token입니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_OF_BLACK_LIST("auth_access_004", "해당 Access Token은 BlackList 토큰입니다.", HttpStatus.CONFLICT),

    REFRESH_TOKEN_NOT_FOUND("auth_refresh_001", "Refresh Token을 요청받지 못했습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("auth_refresh_002", "만료된 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("auth_refresh_003", "유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_IN_REDIS("auth_refresh_004", "Refresh Token이 Redis에 존재하지 않습니다.", HttpStatus.UNAUTHORIZED),

    // common
    UNAUTHORIZED("unauthorized", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    INTERNAL_SERVER_ERROR("internal_server_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
