package com.mylogisticcba.iam.security.auth.services;

import com.mylogisticcba.iam.security.auth.dtos.response.AuthResponse;
import com.mylogisticcba.iam.security.auth.dtos.req.LoginRequest;
import com.mylogisticcba.iam.security.auth.dtos.req.RegisterOwnerRequest;
import com.mylogisticcba.iam.security.auth.dtos.response.LoginResponse;

import java.util.UUID;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest) ;
    AuthResponse registerTenantAndOwner(RegisterOwnerRequest request);
    AuthResponse verifyRegistrationInTenant(String token, String idTenant);
    AuthResponse logoutAllSession(UUID userId, UUID idTenant);
    AuthResponse logoutSession(String refreshToken) ;
    AuthResponse verifyRegistrationTenantAndOwner(String token, String idTenant);
    LoginResponse rotateRefreshToken(String cookieToken);

    AuthResponse verifyAfterFreeze(String token, String tenantName, String username);
}
