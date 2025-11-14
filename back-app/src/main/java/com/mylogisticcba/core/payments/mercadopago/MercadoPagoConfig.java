package com.mylogisticcba.core.payments.mercadopago;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoConfig {

    @Value("${mercadopago.access-token:REPLACE_ME}")
    private String accessToken;

    @Value("${mercadopago.base-url:https://api.mercadopago.com}")
    private String baseUrl;

    public String getAccessToken() {
        return accessToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

