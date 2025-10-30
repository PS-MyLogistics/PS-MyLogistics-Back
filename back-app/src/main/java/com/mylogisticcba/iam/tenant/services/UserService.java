package com.mylogisticcba.iam.tenant.services;

import com.mylogisticcba.iam.security.auth.dtos.req.RegisterOwnerRequest;
import com.mylogisticcba.iam.tenant.dtos.EditUserInTenantRequest;
import com.mylogisticcba.iam.tenant.dtos.RegisterUserInTenantRequest;
import com.mylogisticcba.iam.tenant.dtos.UserDto;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.entity.UserEntity;

import java.util.List;
import java.util.UUID;

public interface UserService  {

    UserEntity createUserOwner(RegisterOwnerRequest req, TenantEntity tenant);
    UserDto createUserInTenant(RegisterUserInTenantRequest request);
    List<UserDto> getUsersByTenant();
    UserEntity getUserByIdAndTenantId(UUID userId, UUID tenantId);
    UserEntity getUserByUsernameAndTenant(String username,UUID tenanId);
    UserEntity getUserByUsernameAndEmailAndTenantId(String user , String email, UUID tenanId);
    UserEntity getUserByEmailAndTenantId(String email, UUID tenantId);
    UserDto editUserDealerInTenant(EditUserInTenantRequest req);
}
