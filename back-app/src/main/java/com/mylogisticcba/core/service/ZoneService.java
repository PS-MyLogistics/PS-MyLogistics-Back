package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.ZoneRequest;
import com.mylogisticcba.core.dto.response.ZoneResponse;

import java.util.List;
import java.util.UUID;

public interface ZoneService {
    List<ZoneResponse> getAllZones();

    ZoneResponse getZoneById(UUID id);

    ZoneResponse createZone(ZoneRequest request);

    ZoneResponse updateZone(UUID id, ZoneRequest request);

    void deleteZone(UUID id);
}