package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.VehicleRequest;
import com.mylogisticcba.core.dto.response.VehicleResponse;

import java.util.List;
import java.util.UUID;

public interface VehicleService {
    VehicleResponse createVehicle(VehicleRequest request);

    List<VehicleResponse> getAllVehicles();

    VehicleResponse getVehicleById(UUID id);

    VehicleResponse updateVehicle(UUID id, VehicleRequest request);

    void deleteVehicle(UUID id);
}