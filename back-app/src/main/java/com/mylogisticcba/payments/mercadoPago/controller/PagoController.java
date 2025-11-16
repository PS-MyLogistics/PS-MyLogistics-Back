package com.mylogisticcba.payments.mercadoPago.controller;

import com.mylogisticcba.payments.mercadoPago.dto.*;
import com.mylogisticcba.payments.mercadoPago.service.PagoService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/pagos") // Base URL para todas las rutas de pago
@Slf4j
public class PagoController {

    private final PagoService pagoService;
    private final com.mylogisticcba.payments.mercadoPago.service.FacturaPagoOrchestratorService facturaPagoOrchestratorService;

    public PagoController(PagoService pagoService,
                         com.mylogisticcba.payments.mercadoPago.service.FacturaPagoOrchestratorService facturaPagoOrchestratorService) {
        this.pagoService = pagoService;
        this.facturaPagoOrchestratorService = facturaPagoOrchestratorService;
    }

    /**
     * Endpoint para registrar el pago de un pedido/factura.
     * POST /api/pagos
     */
  //  @PostMapping
    //public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody RegistrarPagoRequest request) {
    //  log.info("Recibida solicitud POST /api/pagos para facturaId: {}", request.getFacturaId());
    //   PagoResponse response = pagoService.registrarPago(request);
        
        // Aquí devolvemos 200 OK, ya que el 'registrarPago' puede resultar
        // en ÉXITO o FALLO, pero la operación en sí se completó.
    //  return ResponseEntity.ok(response);
    // }
    /**
     * Endpoint para generar una factura/ticket de un pedido.
     * POST /api/facturacion
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    @PostMapping("/facturaAndpreference")
    public ResponseEntity<PagoResponse> crearFacturaAndPreference(@Valid @RequestBody CrearFacturaYPreferenciaRequest request) {
        log.error("💥 ENTRÉ AL CONTROLLER /api/facturacion");
        PagoResponse response = facturaPagoOrchestratorService.registrarFacturaConPreferenciaPago(request);
        return ResponseEntity.ok(response);
    }

}