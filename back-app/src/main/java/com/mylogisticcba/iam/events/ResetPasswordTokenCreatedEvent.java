package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Builder
@Data
public class ResetPasswordTokenCreatedEvent {

    private UUID tenantId;
    private String token;
    private String tenantName;
    private String username;
    private String email;
    private String phone;

}
