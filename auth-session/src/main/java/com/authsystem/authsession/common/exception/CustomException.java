package com.authsystem.authsession.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorType errorType;

    // 기본 ErrorType
    public CustomException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    // 기본 ErrorType + 커스텀 메시지
    public CustomException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
