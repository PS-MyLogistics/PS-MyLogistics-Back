package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.DistributionCreationRequest;
import com.mylogisticcba.core.dto.req.DistributionStatusUpdateRequest;
import com.mylogisticcba.core.dto.response.DistributionResponse;
import com.mylogisticcba.core.entity.Distribution;
import com.mylogisticcba.core.service.DistributionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/distributions")
public class DistributionController {

    private final DistributionService distributionService;

    public DistributionController(DistributionService distributionService) {
        this.distributionService = distributionService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @PostMapping
    public ResponseEntity<DistributionResponse> createDistribution(@Valid @RequestBody DistributionCreationRequest request) {
        DistributionResponse resp = distributionService.createDistribution(request);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<DistributionResponse> getDistributionById(@PathVariable UUID id) {
        Distribution dist = distributionService.getDistributionById(id);

        DistributionResponse resp = DistributionResponse.builder()
                .id(dist.getId())
                .tenantId(dist.getTenantId())
                .orderIds(dist.getOrders().stream().map(o -> o.getId()).collect(Collectors.toList()))
                .dealerId(dist.getDealer() == null ? null : dist.getDealer().getId())
                .vehicleId(dist.getVehicle() == null ? null : dist.getVehicle().getId())
                .startProgramDateTime(dist.getStartProgramDateTime())
                .endProgramDateTime(dist.getEndProgramDateTime())
                .createdAt(dist.getCreatedAt())
                .status(dist.getStatus() == null ? null : dist.getStatus().name())
                .notes(dist.getNotes())
                .build();

        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<DistributionResponse>> getAllDistribution() {
        List<DistributionResponse> resp = distributionService.getAll();
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAnyRole('DEALER')")
    @GetMapping("/dealer/route/{id}")
    public ResponseEntity<DistributionResponse> getDistributionxIdForDealer(@PathVariable UUID id) {

        DistributionResponse resp = distributionService.getDistributionByIdDealer(id);
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAnyRole('DEALER')")
    @GetMapping("/dealer/myDistributions")
    public ResponseEntity<List<DistributionResponse>> getMyDistributions() {
        List<DistributionResponse> resp = distributionService.getDistributionsForDealer();
        return ResponseEntity.ok(resp);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<DistributionResponse> updateDistributionStatus(
            @PathVariable("id") UUID id,
            @RequestBody DistributionStatusUpdateRequest request) {
        DistributionResponse resp = distributionService.updateDistributionStatus(id, request);
        return ResponseEntity.ok(resp);
    }

}
