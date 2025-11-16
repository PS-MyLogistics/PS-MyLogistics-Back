package com.mylogisticcba.iam.repositories;

import org.springframework.stereotype.Repository;
//repostory jpa of verification token entity
import com.mylogisticcba.iam.security.auth.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface VerificarionTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByTokenAndTenantId( UUID token, UUID tenantId);

}
