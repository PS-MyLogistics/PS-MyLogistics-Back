package com.mylogisticcba.iam.security.auth.services.impls;

import com.mylogisticcba.iam.repositories.RefreshTokenRepository;
import com.mylogisticcba.iam.repositories.TenantRepository;
import com.mylogisticcba.iam.repositories.UserRepository;
import com.mylogisticcba.iam.security.auth.entity.RefreshToken;
import com.mylogisticcba.iam.security.auth.exceptions.AuthServiceException;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.tenant.services.impl.TenantConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
@RequiredArgsConstructor
@Service
public class RefreshTokenService implements com.mylogisticcba.iam.security.auth.services.RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;
    private final TenantConfigService tenantConfigService;
    private final UserRepository userRepository;
    //TODO: cambiar a configuracion por tenant
    private static final Instant REFRESH_TOKEN_DURATION_MS = Instant.now().plus(7, ChronoUnit.DAYS);
    private final TenantRepository tenantRepository;

    public RefreshToken createRefreshToken(UUID userId,UUID tenantId) {

        refreshTokenRepository.deleteAllByUserIdAndTenantId(userId, tenantId);
        return refreshTokenRepository.save(RefreshToken.builder()
                .tenantId(tenantId)
                .userId(userId)
                .token(UUID.randomUUID())
                .expiryDate(REFRESH_TOKEN_DURATION_MS)
                .revoked(false)
                .build());
    }

    public RefreshToken findByToken(UUID token) {
        return refreshTokenRepository.findByTokenAndRevoked(token,false)
                .orElseThrow(() -> new AuthServiceException("Refresh token not found is deleted or revoked"));
    }
    public RefreshToken findByUserAndTenantName(String user,String tenantName) {

        TenantEntity tenant=tenantRepository.findByName(tenantName)
                .orElseThrow(() -> new AuthServiceException("Tenant not found"));
        UserEntity userEntity = userRepository.findByUsernameAndTenant_Id(user,tenant.getId())
                .orElseThrow(() -> new AuthServiceException("User not found"));
       return refreshTokenRepository.findByUserIdAndTenantId(userEntity.getId(),tenant.getId())
               .orElseThrow(() -> new AuthServiceException("Refresh token not found is deleted or revoked"));
    }

    public RefreshToken findByTokenAndRevokedFalse(UUID token) {
        return refreshTokenRepository.findByTokenAndRevoked(token,false)
                .orElseThrow(() -> new AuthServiceException("Refresh token not found is deleted or revoked"));

    }

    public void verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            token.setRevoked(true);
            token.setDeletedAt(Instant.now());
            refreshTokenRepository.save(token);
            throw new AuthServiceException("Refresh token expired. Please login again");
        }

    }

    public RefreshToken rotateToken(RefreshToken token) {
        UUID userId = token.getUserId();
        UUID tenantId = token.getTenantId();
        refreshTokenRepository.delete(token);
        return createRefreshToken(userId, tenantId);
    }

    public void revokeToken(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    public void deleteAllByUserIdAndTenantId(UUID userId, UUID tenantId) {
        refreshTokenRepository.deleteAllByUserIdAndTenantId(userId, tenantId);
    }

}
