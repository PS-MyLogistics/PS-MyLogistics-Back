package com.mylogisticcba.iam.security.auth.services;

import com.mylogisticcba.iam.security.auth.entity.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(UUID userId, UUID tenantId);
}
