package com.mylogisticcba.payments.mercadoPago.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO simulado que enviaríamos al gateway de pagos
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoExternoRequest {
    private Double amount;
    private String paymentMethodId; // ej: "visa"
    private String cardToken; // Token de la tarjeta (no la tarjeta real)
}