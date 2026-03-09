package com.authsystem.authsession.user.controller;

import com.authsystem.authsession.auth.security.principal.PrincipalDetails;
import com.authsystem.authsession.common.domain.dto.ApiResponse;
import com.authsystem.authsession.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tests")
public class TestController {
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<Long>> testUser(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = principal.getUser();
        System.out.println(user.toString());
        Long id = user.getId();

        return ApiResponse.of(HttpStatus.OK, null, "유저 테스트 성공", id);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> testAdmin(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = principal.getUser();
        System.out.println(user.toString());
        Long id = user.getId();

        return ApiResponse.of(HttpStatus.OK, null, "관리자 테스트 성공", id);
    }
}
