package com.mylogisticcba.payments.mercadoPago.dto;

import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * DTO de respuesta para un Pago.
 */

@Data
@Builder
@AllArgsConstructor
public class PagoResponse {
    
    private UUID id;
    private Double monto;
    private LocalDateTime fechaPago;
    private EstadoPago estadoPago;
    private String transaccionIdExterno;
    private String preferenciaPagoId;
    private String initPoint; // ⬅️ AGREGAR ESTO
    // No incluimos la 'Factura' completa para evitar bucles infinitos de JSON
    private UUID facturaId;
    public PagoResponse() {}
}