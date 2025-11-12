package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.ZoneRequest;
import com.mylogisticcba.core.dto.response.ZoneResponse;
import com.mylogisticcba.core.entity.Zone;
import com.mylogisticcba.core.repository.orders.ZoneRepository;
import com.mylogisticcba.core.service.ZoneService;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ZoneServiceImpl implements ZoneService {

    private static final Logger log = LoggerFactory.getLogger(ZoneServiceImpl.class);

    private final ZoneRepository zoneRepository;

    public ZoneServiceImpl(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @Override
    public List<ZoneResponse> getAllZones() {
        UUID tenantId = TenantContextHolder.getTenant();
        return zoneRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ZoneResponse getZoneById(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Zone zone = zoneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Zone not found with id: " + id));

        return toResponse(zone);
    }

    @Override
    @Transactional
    public ZoneResponse createZone(ZoneRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();

        // Validar que no exista una zona con el mismo nombre en el tenant
        if (zoneRepository.existsByNameAndTenantId(request.getName(), tenantId)) {
            throw new IllegalArgumentException("Ya existe una zona con el nombre: " + request.getName());
        }

        Zone zone = Zone.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Zone saved = zoneRepository.save(zone);
        log.info("Zone created with id: {} for tenant: {}", saved.getId(), tenantId);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ZoneResponse updateZone(UUID id, ZoneRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();
        Zone zone = zoneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Zone not found with id: " + id));

        // Validar que no exista otra zona con el mismo nombre (excepto la actual)
        if (!zone.getName().equals(request.getName()) &&
            zoneRepository.existsByNameAndTenantId(request.getName(), tenantId)) {
            throw new IllegalArgumentException("Ya existe una zona con el nombre: " + request.getName());
        }

        zone.setName(request.getName());
        zone.setDescription(request.getDescription());
        zone.setColor(request.getColor());
        if (request.getIsActive() != null) {
            zone.setIsActive(request.getIsActive());
        }

        Zone updated = zoneRepository.save(zone);
        log.info("Zone updated with id: {} for tenant: {}", updated.getId(), tenantId);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteZone(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        Zone zone = zoneRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Zone not found with id: " + id));

        zoneRepository.delete(zone);
        log.info("Zone deleted with id: {} for tenant: {}", id, tenantId);
    }

    private ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
                .id(zone.getId())
                .tenantId(zone.getTenantId())
                .name(zone.getName())
                .description(zone.getDescription())
                .color(zone.getColor())
                .isActive(zone.getIsActive())
                .createdAt(zone.getCreatedAt())
                .updatedAt(zone.getUpdatedAt())
                .build();
    }
}