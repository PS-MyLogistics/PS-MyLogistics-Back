package com.mylogisticcba.iam.tenant.services.impl;
import com.mylogisticcba.iam.repositories.TenantRepository;
import com.mylogisticcba.iam.repositories.UserRepository;
import com.mylogisticcba.iam.security.auth.exceptions.AuthServiceException;
import com.mylogisticcba.iam.security.auth.dtos.req.RegisterOwnerRequest;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.iam.tenant.dtos.TenantInfo;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;
import com.mylogisticcba.iam.tenant.enums.PlanType;
import com.mylogisticcba.iam.tenant.enums.TenantStatus;
import com.mylogisticcba.iam.tenant.exceptions.TenantServiceException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class TenantService  implements com.mylogisticcba.iam.tenant.services.TenantService {

    private final TenantConfigService tenantConfigService;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final UserService userService;
    @Qualifier("defaultModelMapper")
    private final ModelMapper modelMapper;
    public TenantEntity createTenant(RegisterOwnerRequest req) throws AuthServiceException {



        // validate if tenant email is unique
        if (tenantRepository.existsByContactEmail((req.getEmail()))) {
            throw new AuthServiceException("Tenant with this email already exists");
        }
        //validate if tenant name is unique
        if (tenantRepository.existsByName(req.getTenantName())) {
            throw new AuthServiceException("Tenant with this name already exists");
        }


        TenantEntity tenant = new TenantEntity();
        tenant.setName(req.getTenantName());
        tenant.setContactEmail(req.getTenantContactEmail());
        tenant.setContactPhone(req.getTenantContactPhone());
        tenant.setAddress(req.getTenantAddress());
        tenant.setPlanType(PlanType.FREE);
        tenant.setMaxUsers(maxUsersForPlanType(PlanType.FREE, null));
        tenant.setStatus(TenantStatus.PENDING_VERIFICATION);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());
        tenant.setActive(true);

        //save tenant
        TenantEntity savedTenant = tenantRepository.save(tenant);
        return savedTenant;



    }

    public Integer maxUsersForPlanType(PlanType planType, UUID tenantId) {

        Integer maxUsersConfigSaved = tenantConfigService.getIntValue(tenantId,"maxUsers", 0);

        if(maxUsersConfigSaved != null && maxUsersConfigSaved > 0) {
            return maxUsersConfigSaved;
        }
        switch (planType) {
            case FREE:
                return 4;
            case PREMIUM:
                return 15;
            case ENTERPRISE:
                return 25;

            default:
                throw new AuthServiceException("Invalid plan type: " + planType);
        }
    }


    public TenantEntity updateTenant(TenantEntity tenant) {
        if (tenant.getId() == null) {
            throw new AuthServiceException("Tenant ID is required for update");
        }
        // Check if the tenant exists
        TenantEntity existingTenant = tenantRepository.findById(tenant.getId())
                .orElseThrow(() -> new AuthServiceException("Tenant not found"));

        // Update fields
        existingTenant.setName(tenant.getName());
        existingTenant.setContactEmail(tenant.getContactEmail());
        existingTenant.setContactPhone(tenant.getContactPhone());
        existingTenant.setAddress(tenant.getAddress());
        existingTenant.setPlanType(tenant.getPlanType());
        existingTenant.setMaxUsers(maxUsersForPlanType(tenant.getPlanType(), tenant.getId()));
        existingTenant.setOwnerId(tenant.getOwnerId());
        existingTenant.setPlanType(tenant.getPlanType());
        existingTenant.setActive(tenant.isActive());
        existingTenant.setStatus(tenant.getStatus());
        existingTenant.setUpdatedAt(tenant.getUpdatedAt() != null ? tenant.getUpdatedAt() : existingTenant.getUpdatedAt());

        // Save updated tenant
        return tenantRepository.save(existingTenant);
    }


    public TenantInfo getInfo() {
        UUID tenantId = TenantContextHolder.getTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new AuthServiceException("Tenant not found"));

        //use modelmapper to map tenant to tenantInfo
        TenantInfo info =  modelMapper.map(tenant, TenantInfo.class);
        UserEntity user = userService.getUserByIdAndTenantId(tenant.getOwnerId(), tenant.getId());
        info.setOwnerName(user.getUsername());
        return info;


    }
    public TenantEntity getTenantById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new TenantServiceException("Not found tenant by id "));
    }

    public TenantEntity getTenantByName(String tenantName) {
        return tenantRepository.findByName(tenantName)
                .orElseThrow(() -> new TenantServiceException("Not found tenant by name "));
    }

}
