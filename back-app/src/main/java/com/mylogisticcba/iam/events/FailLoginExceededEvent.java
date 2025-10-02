package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class FailLoginExceededEvent {
    private UUID tenantId;
    private String tenantName;
    private String username;
    private String email;
    private Integer attemptCount;
    private Instant lastAttemptTime;
}
