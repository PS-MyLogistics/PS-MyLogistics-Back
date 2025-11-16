package com.mylogisticcba.payments.mercadoPago.dto.mapper;


import com.mylogisticcba.payments.mercadoPago.dto.CrearFacturaRequest;
import com.mylogisticcba.payments.mercadoPago.dto.FacturaResponse;
import com.mylogisticcba.payments.mercadoPago.dto.PagoResponse;
import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import com.mylogisticcba.payments.mercadoPago.entity.Pago;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Componente encargado de mapear DTOs a Entidades y viceversa.
 */
@Component // Lo marcamos como Componente para poder inyectarlo después
public class FacturaMapper {

    // --- Mapeo de DTO de Request a Entidad ---

    /**
     * Convierte un DTO de creación en una Entidad Factura lista para guardar.
     * @param request El DTO de entrada
     * @return La Entidad Factura
     */
    public Factura toEntity(CrearFacturaRequest request) {
        Factura factura = new Factura();

        // Obligatorios (NOT NULL en la entidad)
        factura.setTenantId(request.getTenantId());
        factura.setClienteId(request.getClienteId()); // FALTABA
        factura.setTotal(request.getTotal());
        factura.setMetodoPago(request.getMetodoPago());

        // Estado inicial por defecto
        factura.setEstado(EstadoFactura.PENDIENTE);

        // numeroFactura → se genera en el service (correcto)
        // fechaEmision → la genera @CreationTimestamp (correcto)
        return factura;
    }

    // --- Mapeo de Entidad a DTO de Response ---

    /**
     * Convierte una Entidad Factura en un DTO de respuesta seguro.
     * @param factura La Entidad de la BBDD
     * @return El DTO de respuesta
     */
    public FacturaResponse toResponse(Factura factura) {
        FacturaResponse response = new FacturaResponse();
        response.setId(factura.getId());
        response.setTenantId(factura.getTenantId());
        response.setClientId(factura.getClienteId());
        response.setNumeroFactura(factura.getNumeroFactura());
        response.setFechaEmision(factura.getFechaEmision());
        response.setTotal(factura.getTotal());
        response.setMetodoPago(factura.getMetodoPago());
        response.setEstado(factura.getEstado());

        // Mapeamos también la lista de pagos asociados
        if (factura.getPagos() != null && !factura.getPagos().isEmpty()) {
            response.setPagos(toPagoResponseList(factura.getPagos()));
        }

        return response;
    }

    /**
     * Convierte una Entidad Pago en un DTO de respuesta seguro.
     * @param pago La Entidad de la BBDD
     * @return El DTO de respuesta
     */
    public PagoResponse toResponse(Pago pago) {
        PagoResponse response = new PagoResponse();
        response.setId(pago.getId());
        response.setMonto(pago.getMonto());
        response.setFechaPago(pago.getFechaPago());
        response.setEstadoPago(pago.getEstadoPago());
        response.setTransaccionIdExterno(pago.getTransaccionIdExterno());
        response.setFacturaId(pago.getFactura().getId());
        return response;
    }

    /**
     * Convierte una lista de Entidades Factura en una lista de DTOs.
     */
    public List<FacturaResponse> toFacturaResponseList(List<Factura> facturas) {
        return facturas.stream()
                .map(this::toResponse) // Reutiliza el método de arriba
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de Entidades Pago en una lista de DTOs.
     */
    public List<PagoResponse> toPagoResponseList(List<Pago> pagos) {
        return pagos.stream()
                .map(this::toResponse) // Reutiliza el método de arriba
                .collect(Collectors.toList());
    }
}