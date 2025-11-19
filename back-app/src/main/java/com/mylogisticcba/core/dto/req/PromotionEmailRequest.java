package com.mylogisticcba.core.dto.req;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionEmailRequest {

    @NotNull(message = "La lista de clientes no puede ser nula")
    @NotEmpty(message = "Debe seleccionar al menos un cliente")
    private List<UUID> customerIds;

    @NotEmpty(message = "El asunto del email no puede estar vacío")
    @Size(max = 200, message = "El asunto no puede exceder 200 caracteres")
    private String subject;

    @NotEmpty(message = "El mensaje no puede estar vacío")
    private String message;
}
