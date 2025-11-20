package com.mylogisticcba.payments.mercadoPago.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RegistrarPagoRequest {

    private UUID facturaId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;

    /**
     * Token de la tarjeta generado en el frontend usando Mercado Pago SDK
     * Documentación: https://www.mercadopago.com.ar/developers/es/docs/checkout-api/integration-configuration/card-payment-tokenization
     */
    //@NotNull(message = "El token de la tarjeta es obligatorio")
   // private String transaccionIdExterno; // Este es el token de MP

    /*
     * Método de pago: "visa", "master", "amex", etc.
     */
            /*
    @NotNull(message = "El método de pago es obligatorio")
    private String metodoPago;
*/
    /**
     * Email del pagador
     */
    @NotNull(message = "El email del pagador es obligatorio")
    @Email(message = "El email debe ser válido")
    private String emailPagador;

    /**
     * DNI del pagador
     */
    @NotNull(message = "El DNI del pagador es obligatorio")
    private String dniPagador;

    //debe ser mayor a 0
    @Positive(message = "Los meses deben ser mayores a 0")
    private int meses;
}