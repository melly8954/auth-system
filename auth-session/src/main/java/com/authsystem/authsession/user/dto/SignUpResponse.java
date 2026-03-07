package com.authsystem.authsession.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String nickname;
    private LocalDateTime createdAt;
}
