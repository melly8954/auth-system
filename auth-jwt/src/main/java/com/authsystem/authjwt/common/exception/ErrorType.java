package com.authsystem.authjwt.common.exception;

import com.nimbusds.openid.connect.sdk.assurance.IdentityAssuranceProcess;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    // 공통
    BAD_REQUEST("bad_request", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("unauthorized", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("not_found", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("conflict", "요청이 현재 상태와 충돌합니다.", HttpStatus.CONFLICT),
    INTERNAL_ERROR("internal_error", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // user
    DUPLICATE_USERNAME("USER-001", "이미 존재하는 아이디입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_EMAIL("USER-002", "이미 존재하는 이메일입니다.", HttpStatus.BAD_REQUEST),

    // auth
    REFRESH_TOKEN_NOT_FOUND("auth-001", "Refresh Token을 요청받지 못했습니다.", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_EXPIRED("auth-002", "만료된 Refresh Token입니다.", HttpStatus.CONFLICT),
    REFRESH_TOKEN_INVALID("auth-003", "유효하지 않은 Refresh Token입니다.", HttpStatus.CONFLICT),
    REFRESH_TOKEN_NOT_IN_REDIS("auth-004", "Refresh Token이 Redis에 존재하지 않습니다.", HttpStatus.NOT_FOUND);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;
}
