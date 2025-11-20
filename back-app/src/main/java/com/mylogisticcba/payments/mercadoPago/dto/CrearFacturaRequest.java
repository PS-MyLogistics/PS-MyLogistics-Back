package com.mylogisticcba.payments.mercadoPago.dto;



import com.mylogisticcba.payments.mercadoPago.model.enums.MetodoPago;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para la creación de una nueva factura.
 * Esto es lo que 'pedido-service' (u otro) enviará.
 */
@Data
public class CrearFacturaRequest {

    @NotNull(message = "El ID del tenant no puede ser nulo")
    private UUID tenantId;

    @NotNull(message = "El ID del cliente no puede ser nulo")
    private UUID clienteId;

    @NotNull(message = "El monto total no puede ser nulo")
    @Min(value = 0, message = "El monto total debe ser mayor o igual a 0")
    private Double total;

}