package com.authsystem.authjwt.common.exception;

import com.authsystem.authjwt.common.domain.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorType errorType = e.getErrorType();
        log.error("비즈니스 로직 예외 발생 - Code: {}, Message: {}", errorType.getErrorCode(), errorType.getMessage());

        return ApiResponse.of(
                errorType.getHttpStatus(),
                errorType.getErrorCode(),
                e.getMessage(),
                null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("권한 거부 예외 발생 - Message: {}", e.getMessage());

        return ApiResponse.of(
                ErrorType.FORBIDDEN.getHttpStatus(),
                ErrorType.FORBIDDEN.getErrorCode(),
                ErrorType.FORBIDDEN.getMessage(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("서버 내부 예외 발생 - Message: {}", e.getMessage());

        return ApiResponse.of(
                ErrorType.INTERNAL_ERROR.getHttpStatus(),
                ErrorType.INTERNAL_ERROR.getErrorCode(),
                ErrorType.INTERNAL_ERROR.getMessage(),
                null
        );
    }
}
