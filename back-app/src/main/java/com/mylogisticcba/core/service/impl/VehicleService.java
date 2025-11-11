package com.mylogisticcba.core.service.impl;

import com.mylogisticcba.core.dto.req.VehicleRequest;
import com.mylogisticcba.core.dto.response.VehicleResponse;
import com.mylogisticcba.core.entity.Vehicle;
import com.mylogisticcba.core.repository.distribution.VehicleRepository;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VehicleService implements com.mylogisticcba.core.service.VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        // Verificar que la placa no exista ya
        if (vehicleRepository.existsByPlate(request.getPlate())) {
            throw new DataIntegrityViolationException("Vehicle with plate " + request.getPlate() + " already exists");
        }

        Vehicle vehicle = Vehicle.builder()
                .tenantId(tenantId)
                .plate(request.getPlate())
                .model(request.getModel())
                .capacity(request.getCapacity())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        return mapToResponse(saved);
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        UUID tenantId = TenantContextHolder.getTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        return vehicleRepository.findAll().stream()
                .filter(vehicle -> vehicle.getTenantId().equals(tenantId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getVehicleById(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        if (!vehicle.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Vehicle not found in this tenant");
        }

        return mapToResponse(vehicle);
    }

    @Transactional
    @Override
    public VehicleResponse updateVehicle(UUID id, VehicleRequest request) {
        UUID tenantId = TenantContextHolder.getTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        if (!vehicle.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Vehicle not found in this tenant");
        }

        // Verificar si la nueva placa ya existe en otro vehículo
        if (!vehicle.getPlate().equals(request.getPlate()) &&
            vehicleRepository.existsByPlate(request.getPlate())) {
            throw new DataIntegrityViolationException("Vehicle with plate " + request.getPlate() + " already exists");
        }

        vehicle.setPlate(request.getPlate());
        vehicle.setModel(request.getModel());
        vehicle.setCapacity(request.getCapacity());

        Vehicle updated = vehicleRepository.save(vehicle);

        return mapToResponse(updated);
    }

    @Transactional
    @Override
    public void deleteVehicle(UUID id) {
        UUID tenantId = TenantContextHolder.getTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        if (!vehicle.getTenantId().equals(tenantId)) {
            throw new EntityNotFoundException("Vehicle not found in this tenant");
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .tenantId(vehicle.getTenantId())
                .plate(vehicle.getPlate())
                .model(vehicle.getModel())
                .capacity(vehicle.getCapacity())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}