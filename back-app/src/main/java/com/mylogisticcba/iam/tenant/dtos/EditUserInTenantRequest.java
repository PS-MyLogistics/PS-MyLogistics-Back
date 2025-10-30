package com.mylogisticcba.iam.tenant.dtos;

import com.mylogisticcba.iam.tenant.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class EditUserInTenantRequest {

    private UUID userId;
    @NotBlank(message = "Username is required")
    private String username;

    private List<Role> roles;

    private String telephone;

    private String address;

    private String city;

    private String stateOrProvince;

    private String newPassword;
}
