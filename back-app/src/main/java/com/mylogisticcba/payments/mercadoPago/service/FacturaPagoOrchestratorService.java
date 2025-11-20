package com.mylogisticcba.payments.mercadoPago.service;

import com.mylogisticcba.payments.mercadoPago.dto.CrearFacturaYPreferenciaRequest;
import com.mylogisticcba.payments.mercadoPago.dto.PagoResponse;

public interface FacturaPagoOrchestratorService {
    PagoResponse registrarFacturaConPreferenciaPago(CrearFacturaYPreferenciaRequest request,Integer meses);


}

