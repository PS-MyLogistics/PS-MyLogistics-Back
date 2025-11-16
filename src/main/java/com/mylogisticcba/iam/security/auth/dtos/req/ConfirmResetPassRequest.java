package com.mylogisticcba.iam.security.auth.dtos.req;

import lombok.Data;

@Data
public class ConfirmResetPassRequest {

    private String token;
    private String newPassword;
    private String username;
    private String tenantName;






}
