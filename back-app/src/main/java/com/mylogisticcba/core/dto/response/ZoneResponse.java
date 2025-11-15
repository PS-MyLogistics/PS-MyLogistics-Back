package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZoneResponse {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String description;
    private String color;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}