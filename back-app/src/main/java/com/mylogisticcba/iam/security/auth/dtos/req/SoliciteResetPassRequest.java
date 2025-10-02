package com.mylogisticcba.iam.security.auth.dtos.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SoliciteResetPassRequest {

    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Tenant name is required")
    private String tenantName;
    @NotBlank(message = "email is required")
    private String email;
}
