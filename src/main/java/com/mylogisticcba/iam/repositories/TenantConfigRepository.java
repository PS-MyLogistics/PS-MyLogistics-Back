package com.mylogisticcba.iam.repositories;

import com.mylogisticcba.iam.tenant.entity.TenantConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TenantConfigRepository extends JpaRepository<TenantConfigEntity, UUID> {
        List<TenantConfigEntity> findByTenantId(UUID tenantId);

}
