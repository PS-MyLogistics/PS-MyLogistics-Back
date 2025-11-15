package com.mylogisticcba.core.service;

import com.mylogisticcba.core.dto.req.DistributionCreationRequest;
import com.mylogisticcba.core.dto.req.DistributionStatusUpdateRequest;
import com.mylogisticcba.core.dto.response.DistributionResponse;
import com.mylogisticcba.core.entity.Distribution;

import java.util.List;
import java.util.UUID;

public interface DistributionService {
    // ahora devuelve DTO en lugar de entidad
    DistributionResponse createDistribution(DistributionCreationRequest req);

    // Guarda la secuencia optimizada de órdenes para una distribution y devuelve la entidad actualizada
    com.mylogisticcba.core.entity.Distribution saveOptimizedSequence(UUID distributionId, List<UUID> optimizedOrderIds);

    // Recupera una distribution por id (valida tenant en implementación)
    com.mylogisticcba.core.entity.Distribution getDistributionById(UUID distributionId);

    List<DistributionResponse> getAll();

    DistributionResponse getDistributionByIdDealer(UUID id);

    List<DistributionResponse> getDistributionsForDealer();

    // Obtiene una distribución por ID con validación de rol (DEALER solo ve las suyas)
    DistributionResponse getDistributionByIdWithRoleValidation(UUID id);

    // Actualiza el estado de una distribución
    DistributionResponse updateDistributionStatus(UUID id, DistributionStatusUpdateRequest request);
}
