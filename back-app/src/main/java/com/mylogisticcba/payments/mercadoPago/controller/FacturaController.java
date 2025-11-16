package com.mylogisticcba.payments.mercadoPago.controller;

import com.mylogisticcba.payments.mercadoPago.dto.CrearFacturaRequest;
import com.mylogisticcba.payments.mercadoPago.dto.FacturaResponse;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import com.mylogisticcba.payments.mercadoPago.service.FacturaService;
import jakarta.validation.Valid; // Importante para la validación
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/facturacion") // Base URL para todas las rutas de factura
@Slf4j
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /**
     * Endpoint para generar una factura/ticket de un pedido.
     * POST /api/facturacion
     */
   // @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
   // @PostMapping
   // public ResponseEntity<FacturaResponse> crearFactura(@Valid @RequestBody CrearFacturaRequest request) {
    // FacturaResponse response = facturaService.crearFactura(request);
        // Devuelve 201 Created y la factura creada en el cuerpo
       //     return ResponseEntity.ok(response);
    //}


    /**
     * Endpoint para consultar una factura por su ID.
     * GET /api/facturacion/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> obtenerFacturaPorId(@PathVariable UUID id) {
        log.info("Recibida solicitud GET /api/facturacion/{}", id);
        FacturaResponse response = facturaService.obtenerFacturaPorId(id);
        return ResponseEntity.ok(response);
    }


    /**
     * Endpoint para listar facturas, con filtro opcional por estado.
     * GET /api/facturacion?estado=PENDIENTE
     */
    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listarFacturas(
            @RequestParam(required = false) EstadoFactura estado) {
        
        if (estado != null) {
            log.info("Recibida solicitud GET /api/facturacion?estado={}", estado);
            return ResponseEntity.ok(facturaService.listarFacturasPorEstado(estado));
        }
        
        // Si no hay filtro, podríamos devolver todas (aunque no está en el 'Service',
        // por ahora nos limitamos a lo que sí está).
        log.warn("Solicitud GET /api/facturacion sin filtro de estado no implementada, devolviendo lista vacía.");
        return ResponseEntity.ok(List.of()); // O implementar un 'findAll'
    }

    /**
     * Endpoint para anular una factura.
     * PUT /api/facturacion/{id}/anular
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<FacturaResponse> anularFactura(@PathVariable UUID id) {
        log.info("Recibida solicitud PUT /api/facturacion/{}/anular", id);
        FacturaResponse response = facturaService.anularFactura(id);
        return ResponseEntity.ok(response);
    }
}