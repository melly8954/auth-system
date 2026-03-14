package com.authsystem.authjwt.admin;

import com.authsystem.authjwt.auth.security.principal.PrincipalDetails;
import com.authsystem.authjwt.common.domain.dto.ApiResponse;
import com.authsystem.authjwt.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class AdminTestController {
    @GetMapping("")
    public ResponseEntity<ApiResponse<Long>> testUser(@AuthenticationPrincipal PrincipalDetails principal) {
        User user = principal.getUser();
        System.out.println(user.toString());
        Long id = user.getId();

        return ApiResponse.of(HttpStatus.OK, null, "관리자 URL 접근 성공", id);
    }
}
