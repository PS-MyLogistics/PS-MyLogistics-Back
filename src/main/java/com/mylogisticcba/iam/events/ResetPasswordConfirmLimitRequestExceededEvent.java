package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Builder
@Data
public class ResetPasswordConfirmLimitRequestExceededEvent {
    private UUID tenantId;
    private String tenantName;
    private String username;
    private String email;
    private String attemptCount;
    private LocalDateTime lastAttemptTime;
    private String location;
    private String ipAddress;
}
