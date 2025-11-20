package com.mylogisticcba.payments.mercadoPago.service.impl;

import com.mylogisticcba.payments.mercadoPago.dto.CrearFacturaYPreferenciaRequest;
import com.mylogisticcba.payments.mercadoPago.dto.FacturaResponse;
import com.mylogisticcba.payments.mercadoPago.dto.PagoResponse;
import com.mylogisticcba.payments.mercadoPago.dto.RegistrarPagoRequest;
import com.mylogisticcba.payments.mercadoPago.service.FacturaPagoOrchestratorService;
import com.mylogisticcba.payments.mercadoPago.service.FacturaService;
import com.mylogisticcba.payments.mercadoPago.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaPagoOrchestratorServiceImpl implements FacturaPagoOrchestratorService {

    private final FacturaService facturaService;
    private final PagoService pagoService;

    @Override
    public PagoResponse registrarFacturaConPreferenciaPago(CrearFacturaYPreferenciaRequest request, Integer months) {
        log.info("Orquestador: iniciar flujo crear factura + preferencia");

        if (request == null || request.getRequestFactura() == null) {
            throw new IllegalArgumentException("Request de factura es obligatorio");
        }

        // 1) Crear factura
        FacturaResponse facturaResponse = facturaService.crearFactura(request.getRequestFactura());
        log.info("Orquestador: factura creada id={}", facturaResponse.getId());

        // 2) Preparar request de pago
        RegistrarPagoRequest pagoReq = request.getRequestPago();
        if (pagoReq == null) {
            // No hay request de pago: devolvemos respuesta vacía o lanzamos según política
            throw new IllegalArgumentException("Request de pago es obligatorio");
        }

        pagoReq.setFacturaId(facturaResponse.getId());

        // 3) Delegar a PagoService para crear el pago / preferencia (se ejecuta vía proxy)
        try {
            PagoResponse pagoResponse = pagoService.registrarPago(pagoReq,months);
            log.info("Orquestador: pago registrado, id={}", pagoResponse.getId());
            return pagoResponse;
        } catch (Exception ex) {
            log.error("Orquestador: error al crear preferencia/pago para facturaId={}", facturaResponse.getId(), ex);
            throw ex;
        }
    }
}

