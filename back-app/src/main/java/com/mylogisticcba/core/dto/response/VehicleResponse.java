package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleResponse {
    private UUID id;
    private UUID tenantId;
    private String plate;
    private String model;
    private Integer capacity;
    private Instant createdAt;
    private Instant updatedAt;
}