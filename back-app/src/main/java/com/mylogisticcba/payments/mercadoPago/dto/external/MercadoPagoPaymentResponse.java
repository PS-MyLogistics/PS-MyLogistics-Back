package com.mylogisticcba.payments.mercadoPago.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO que representa la respuesta REAL de Mercado Pago
 * Documentación: https://www.mercadopago.com.ar/developers/es/reference/payments/_payments/post
 */
@Data
public class MercadoPagoPaymentResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("status")
    private String status; // "approved", "rejected", "pending", etc.

    @JsonProperty("status_detail")
    private String statusDetail;

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("date_created")
    private OffsetDateTime dateCreated;

    @JsonProperty("date_approved")
    private OffsetDateTime dateApproved;

    @JsonProperty("payment_method_id")
    private String paymentMethodId;

    @JsonProperty("payment_type_id")
    private String paymentTypeId;

    @JsonProperty("description")
    private String description;
}