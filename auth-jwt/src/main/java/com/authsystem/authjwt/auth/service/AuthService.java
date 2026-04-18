package com.authsystem.authjwt.auth.service;

import com.authsystem.authjwt.auth.dto.ReIssueTokenDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    ReIssueTokenDto reissueToken(String refreshToken, HttpServletResponse response);
    void logout(String BearerToken, String refreshToken, HttpServletResponse response);

}
