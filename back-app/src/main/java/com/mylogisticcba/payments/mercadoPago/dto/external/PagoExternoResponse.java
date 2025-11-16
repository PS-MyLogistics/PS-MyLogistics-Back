package com.mylogisticcba.payments.mercadoPago.dto.external;

import lombok.Data;

// DTO simulado que recibiríamos del gateway de pagos
@Data
public class PagoExternoResponse {
    private String id; // El ID de transacción externo
    private String status; // ej: "approved", "rejected"
    private String detail;
}
