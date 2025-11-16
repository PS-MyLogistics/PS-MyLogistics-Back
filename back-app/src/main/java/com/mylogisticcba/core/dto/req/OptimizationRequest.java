package com.mylogisticcba.core.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationRequest {
    // Lista de órdenes (UUID) cuyos clientes se usarán como ubicaciones
    private List<UUID> orderIds;
    // Opcional: número de vehículos a usar (por defecto 1)
    private Integer vehicleCount;
    // Perfil de routing: driving-car, cycling-regular, foot-walkg
    private String profile;

}
