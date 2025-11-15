package com.mylogisticcba.core.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionCreationRequest {
    private List<UUID> orderIds;
    private UUID dealerId; // usuario responsable (UserEntity.id)
    private UUID vehicleId;
    private LocalDateTime startProgramDateTime;
    private LocalDateTime endProgramDateTime;
    private String notes;
}

