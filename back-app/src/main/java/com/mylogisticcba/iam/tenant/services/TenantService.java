package com.mylogisticcba.iam.tenant.services;

import com.mylogisticcba.iam.security.auth.dtos.req.RegisterOwnerRequest;
import com.mylogisticcba.iam.tenant.dtos.TenantInfo;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.enums.PlanType;

import java.util.UUID;

public interface TenantService {
     TenantEntity createTenant(RegisterOwnerRequest req) ;
     Integer maxUsersForPlanType(PlanType planType, UUID tenantId);
     TenantEntity updateTenant(TenantEntity tenant) ;
     TenantInfo getInfo();
    TenantEntity getTenantById(UUID id);
}
