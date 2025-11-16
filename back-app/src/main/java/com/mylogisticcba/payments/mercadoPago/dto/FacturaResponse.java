package com.mylogisticcba.payments.mercadoPago.dto;


import com.mylogisticcba.payments.mercadoPago.dto.PagoResponse;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import com.mylogisticcba.payments.mercadoPago.model.enums.MetodoPago;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para una Factura.
 * Esto es lo que el cliente verá (seguro y formateado).
 */
@Data
public class FacturaResponse {
    
    private UUID id;
    private UUID tenantId;
    private UUID clientId;
    private String numeroFactura;
    private LocalDateTime fechaEmision;
    private Double total;
    private MetodoPago metodoPago;
    private EstadoFactura estado;
    
    // Incluimos los pagos asociados a esta factura
    private List<PagoResponse> pagos;
}