package com.mylogisticcba.iam.repositories;

import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
    Optional<TenantEntity> findById(UUID id);


    Optional<TenantEntity> findByName(@NotNull @NotBlank String name);

    boolean existsByContactEmail(String attr0);

    boolean existsByName(@NotNull @NotBlank String name);
}

