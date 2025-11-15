package com.mylogisticcba.iam.events;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TenantCreatedEvent {

    private UUID tenantId;
    private String ownerUsername;
    private String ownerEmail;
    private String ownerPhone;
    private UUID tokenVerification;

    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
}
