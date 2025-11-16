package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class ResetPasswordRequestTokenExceededEvent {
    private UUID tenantId;
    private String tenantName;
    private String username;
    private String email;
    private Integer attemptCount;
}
