package com.mylogisticcba.iam.repositories;

import com.mylogisticcba.iam.security.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    void deleteAllByUserIdAndTenantId(UUID userId, UUID tenantId);

    Optional<RefreshToken> findByTokenAndRevoked(UUID token, boolean revoked);

    Optional<RefreshToken> findByUserIdAndTenantId(UUID userId, UUID tenantId);

}
