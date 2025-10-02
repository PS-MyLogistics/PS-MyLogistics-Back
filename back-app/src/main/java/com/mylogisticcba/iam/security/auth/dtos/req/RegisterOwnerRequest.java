package com.mylogisticcba.iam.security.auth.dtos.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class RegisterOwnerRequest extends RegisterRequest {

    //  users camps
    @NotNull(message = "Telephone is required")
    @NotBlank(message = "Telephone cannot be empty")
    private String telephone;

    @NotNull(message = "Address is required")
    @NotBlank(message = "Address cannot be empty")
    private String address;

    @NotNull(message = "City is required")
    @NotBlank(message = "City cannot be empty")
    private String city;

    @NotNull(message = "State or province is required")
    @NotBlank(message = "State or province cannot be empty")
    private String stateOrProvince;

    // tenant camps
    @NotNull(message = "Tenant name is required")
    @NotBlank(message = "Tenant name cannot be empty")
    private String tenantName;

    @NotNull(message = "Tenant contact email is required")
    @NotBlank(message = "Tenant contact email cannot be empty")
    @Email(message = "Tenant contact email must be a valid email address")
    private String tenantContactEmail;

    @NotNull(message = "Tenant contact phone is required")
    @NotBlank(message = "Tenant contact phone cannot be empty")
    private String tenantContactPhone;

    @NotNull(message = "Tenant address is required")
    @NotBlank(message = "Tenant address cannot be empty")
    private String tenantAddress;
}
