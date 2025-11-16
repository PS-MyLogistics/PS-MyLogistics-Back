package com.mylogisticcba.core.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributionStatusUpdateRequest {

    private String status;  // Campo para actualizar el estado de la distribución

}
