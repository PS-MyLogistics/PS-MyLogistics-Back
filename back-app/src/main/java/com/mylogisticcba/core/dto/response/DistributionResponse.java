package com.mylogisticcba.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionResponse {
    private UUID id;
    private UUID tenantId;
    private List<UUID> orderIds;
    private UUID dealerId;
    private UUID vehicleId;
    private LocalDateTime startProgramDateTime;
    private LocalDateTime endProgramDateTime;
    private Instant createdAt;
    private String status;
    private String notes;
    private boolean isOptimized;
    private Map<Integer,UUID>  optimizatedRoute; // Mapa de secuencia de paradas a Order ID
}

