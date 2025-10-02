package com.mylogisticcba.iam.security.auth.dtos.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {

    @NotNull(message = "email is required")
    @NotBlank(message = "email cannot be empty")
    String email;
    @NotNull(message = "password is required")
    @NotBlank(message = "paswword cannot be empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^_&*(),.?\":{}|<>]).{8,}$",
            message = "password must contain at least one lowercase, one uppercase and one special character"
    )
    String password;
    @NotNull(message = "username is required")
    @NotBlank(message = "username cannot be empty")
    String username;

}
