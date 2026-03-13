package com.authsystem.authjwt.user.controller;

import com.authsystem.authjwt.common.domain.dto.ApiResponse;
import com.authsystem.authjwt.user.dto.SignUpRequest;
import com.authsystem.authjwt.user.dto.SignUpResponse;
import com.authsystem.authjwt.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@RequestBody SignUpRequest request) {
        SignUpResponse result = userService.signUp(request);

        return ApiResponse.of(HttpStatus.OK, null, "회원가입 성공", result);
    }
}
