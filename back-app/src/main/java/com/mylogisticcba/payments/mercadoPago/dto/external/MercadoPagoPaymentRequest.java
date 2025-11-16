package com.mylogisticcba.payments.mercadoPago.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO que representa el request REAL de Mercado Pago
 * Documentación: https://www.mercadopago.com.ar/developers/es/reference/payments/_payments/post
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MercadoPagoPaymentRequest {

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("token")
    private String token; // Token de la tarjeta (generado en el frontend)

    @JsonProperty("description")
    private String description;

    @JsonProperty("installments")
    private Integer installments; // Cuotas

    @JsonProperty("payment_method_id")
    private String paymentMethodId; // ej: "visa", "master", etc.

    @JsonProperty("payer")
    private Payer payer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payer {
        @JsonProperty("email")
        private String email;

        @JsonProperty("identification")
        private Identification identification;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Identification {
        @JsonProperty("type")
        private String type; // ej: "DNI", "CUIT"

        @JsonProperty("number")
        private String number;
    }
}