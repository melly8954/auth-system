package com.authsystem.authjwt.auth.controller;

import com.authsystem.authjwt.auth.dto.ReIssueTokenDto;
import com.authsystem.authjwt.auth.service.AuthService;
import com.authsystem.authjwt.common.domain.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReIssueTokenDto>> reissueToken(@CookieValue(name = "RefreshToken", defaultValue = "") String refreshToken,
                                                                     HttpServletResponse response) {
        ReIssueTokenDto responseDto = authService.reissueToken(refreshToken, response);
        return ApiResponse.of(HttpStatus.OK, "null","토큰 재발급 성공", responseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(name = "Authorization", required = false) String bearerToken,
                                                    @CookieValue(name = "RefreshToken", defaultValue = "") String refreshToken,
                                                    HttpServletResponse response) {
        authService.logout(bearerToken, refreshToken, response);
        return ApiResponse.of(HttpStatus.OK, "null","로그아웃 성공", null);
    }
}
