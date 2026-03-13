package com.authsystem.authjwt.user.service;

import com.authsystem.authjwt.user.dto.SignUpRequest;
import com.authsystem.authjwt.user.dto.SignUpResponse;

public interface UserService {
    SignUpResponse signUp(SignUpRequest request);
}
