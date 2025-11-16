package com.mylogisticcba.payments.mercadoPago.config;

import com.mercadopago.MercadoPagoConfig;
import com.mylogisticcba.payments.mercadoPago.config.properties.PaymentGatewayProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoConfiguration {

    private final PaymentGatewayProperties properties;

    /**
     * Configura el SDK de Mercado Pago al iniciar la aplicación
     */
    @PostConstruct
    public void configureMercadoPago() {
        try {
            // Configurar el Access Token globalmente
            MercadoPagoConfig.setAccessToken(properties.getAccessToken());

            // Configurar timeouts (opcional)
            MercadoPagoConfig.setConnectionTimeout(properties.getTimeoutMs());
            MercadoPagoConfig.setConnectionRequestTimeout(properties.getTimeoutMs());
            MercadoPagoConfig.setSocketTimeout(properties.getTimeoutMs());

            log.info("✅ Mercado Pago SDK configurado correctamente");
            log.info("   Base URL: {}", properties.getBaseUrl());
            log.info("   Timeout: {}ms", properties.getTimeoutMs());

        } catch (Exception e) {
            log.error("❌ Error al configurar Mercado Pago SDK", e);
            throw new RuntimeException("No se pudo configurar Mercado Pago", e);
        }
    }
}