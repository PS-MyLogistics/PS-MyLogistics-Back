package com.mylogisticcba.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryStartingRequest {

    @NotNull(message = "El ID del cliente no puede estar vacío")
    private UUID customerId;

    @NotEmpty(message = "El número de pedido no puede estar vacío")
    private String orderNumber;

    @NotNull(message = "La posición estimada no puede estar vacía")
    private Integer estimatedPosition;
}
