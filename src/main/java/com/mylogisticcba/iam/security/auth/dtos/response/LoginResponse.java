package com.mylogisticcba.iam.security.auth.dtos.response;

import com.mylogisticcba.iam.security.auth.entity.RefreshToken;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class LoginResponse {


    String accesToken;
    RefreshToken refreshToken;
    boolean success;
    String message;

}
