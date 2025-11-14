package com.mylogisticcba.core.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateRequest {

    private String status;  // Campo para actualizar el estado
    private String notes;   // Opcionalmente permitir actualizar notas

}
