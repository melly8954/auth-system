package com.authsystem.authjwt.auth.service;

import com.authsystem.authjwt.auth.dto.ReIssueTokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    ReIssueTokenDto reissueToken(HttpServletRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);

}
