package com.mylogisticcba.core.repository.distribution;

import com.mylogisticcba.core.entity.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, UUID> {
    List<Distribution> findAllByTenantId(UUID tenant);
}