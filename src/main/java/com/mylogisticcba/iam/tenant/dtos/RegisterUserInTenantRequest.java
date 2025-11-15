package com.mylogisticcba.iam.tenant.dtos;

import com.mylogisticcba.iam.security.auth.dtos.req.RegisterRequest;
import com.mylogisticcba.iam.tenant.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;
@Data
public class RegisterUserInTenantRequest extends RegisterRequest {
    @NotBlank(message = "roles required")
    List<Role> roles;

    @NotBlank(message = "Telephone cannot be empty")
    @Pattern(regexp = "^[0-9]+$", message = "El celular solo puede contener números")
    String telephone;

    String address;
    String city;
    String stateOrProvince;

}
