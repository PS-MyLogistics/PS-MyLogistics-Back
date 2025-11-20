package com.mylogisticcba.payments.mercadoPago.service;

import com.mylogisticcba.payments.mercadoPago.dto.PagoResponse;
import com.mylogisticcba.payments.mercadoPago.dto.RegistrarPagoRequest;

public interface PagoService {

    /**
     * Procesa un intento de pago para una factura.
     * Esta es la lógica compleja que usará WebClient y CircuitBreaker.
     * @param request DTO con los datos del pago
     * @return El DTO del pago procesado (EXITOSO o FALLIDO)
     */
    PagoResponse registrarPago(RegistrarPagoRequest request,Integer months);


    // ... (aquí podrían ir métodos como 'reembolsarPago', 'obtenerPagoPorId', etc.)
}