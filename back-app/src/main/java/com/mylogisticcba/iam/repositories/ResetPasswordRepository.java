package com.mylogisticcba.iam.repositories;

import com.mylogisticcba.iam.security.auth.entity.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordRepository extends JpaRepository<ResetPasswordToken, UUID> {
    Optional<List<ResetPasswordToken>> findAllByUserIdAndTenantIdAndIsUsedFalse(UUID userId, UUID tenantId);

}
