package com.mylogisticcba.payments.mercadoPago.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@ConfigurationProperties(prefix = "app.external-services.payment-gateway")
@Validated // Valida que las propiedades no sean nulas al arrancar
@Data
public class PaymentGatewayProperties {

    @NotBlank(message = "El access token de Mercado Pago no puede estar vacío")
    private String accessToken;

    @NotBlank(message = "La URL base del gateway de pagos no puede estar vacía")
    private String baseUrl;

    @NotNull(message = "El timeout del gateway de pagos no puede ser nulo")
    private Integer timeoutMs;
}