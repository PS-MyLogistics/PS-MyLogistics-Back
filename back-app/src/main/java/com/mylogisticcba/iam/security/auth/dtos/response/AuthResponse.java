package com.mylogisticcba.iam.security.auth.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    String token;
    boolean success;
    String message;
}
