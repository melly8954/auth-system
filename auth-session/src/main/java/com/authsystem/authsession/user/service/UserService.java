package com.authsystem.authsession.user.service;

import com.authsystem.authsession.user.dto.SignUpRequest;
import com.authsystem.authsession.user.dto.SignUpResponse;

public interface UserService {
    SignUpResponse signUp(SignUpRequest request);
}
