package com.mylogisticcba.iam.repositories;

import com.mylogisticcba.iam.tenant.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsernameAndTenant_Id(String username, UUID tenantId);
    Optional<UserEntity> findByEmailAndTenant_Id(String email, UUID tenantId);
    Optional<UserEntity> findByIdAndTenant_Id(UUID id, UUID tenantId);

    Optional<List<UserEntity>> findByTenant_Id(UUID tenantId);


    boolean existsByUsernameAndOwnerTrue(String username);

    boolean existsByUsernameAndTenant_Id(String email, UUID tenantId);

    boolean existsByEmailAndTenant_Id(String email, UUID tenantId);


    Optional<UserEntity> findByUsernameAndEmailAndTenant_Id(String username, String email, UUID tenantId);
}
