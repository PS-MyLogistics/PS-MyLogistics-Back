package com.mylogisticcba.core.controller;

import com.mylogisticcba.core.dto.req.ZoneRequest;
import com.mylogisticcba.core.dto.response.ZoneResponse;
import com.mylogisticcba.core.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/zones")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/getAll")
    public ResponseEntity<List<ZoneResponse>> getAllZones() {
        List<ZoneResponse> zones = zoneService.getAllZones();
        return ResponseEntity.ok(zones);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'DEALER')")
    @GetMapping("/{id}")
    public ResponseEntity<ZoneResponse> getZoneById(@PathVariable String id) {
        ZoneResponse zone = zoneService.getZoneById(UUID.fromString(id));
        return ResponseEntity.ok(zone);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("/create")
    public ResponseEntity<ZoneResponse> createZone(@Valid @RequestBody ZoneRequest request) {
        ZoneResponse zone = zoneService.createZone(request);
        return ResponseEntity.ok(zone);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ZoneResponse> updateZone(
            @PathVariable String id,
            @Valid @RequestBody ZoneRequest request) {
        ZoneResponse updated = zoneService.updateZone(UUID.fromString(id), request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable String id) {
        zoneService.deleteZone(UUID.fromString(id));
        return ResponseEntity.noContent().build();
    }
}