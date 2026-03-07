package com.authsystem.authsession.user.controller;

import com.authsystem.authsession.common.domain.dto.ApiResponse;
import com.authsystem.authsession.user.dto.SignUpRequest;
import com.authsystem.authsession.user.dto.SignUpResponse;
import com.authsystem.authsession.user.service.UserService;
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
