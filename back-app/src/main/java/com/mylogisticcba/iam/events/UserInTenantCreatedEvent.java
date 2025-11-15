package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Builder
@Data
public class UserInTenantCreatedEvent {

    private UUID tenantId;
    private UUID tokenVerification;
    private String tenantName;
    private String username;
    private String email;
    private String phone;


}
