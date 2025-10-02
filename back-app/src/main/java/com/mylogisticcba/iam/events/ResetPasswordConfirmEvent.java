package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class ResetPasswordConfirmEvent {
    private UUID tenantId;
    private String tenantName;
    private String username;
    private String email;
}
