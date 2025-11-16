package com.mylogisticcba.iam.security.auth.dtos.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaskEmailRequest {

    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Tenant name is required")
    private String tenantName;

}
