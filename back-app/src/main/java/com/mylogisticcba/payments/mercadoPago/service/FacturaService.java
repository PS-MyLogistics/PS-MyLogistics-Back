package com.mylogisticcba.payments.mercadoPago.service;

import com.mylogisticcba.payments.mercadoPago.dto.CrearFacturaRequest;
import com.mylogisticcba.payments.mercadoPago.dto.FacturaResponse;
import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;

import java.util.List;
import java.util.UUID;

public interface FacturaService {

    /**
     * Crea una nueva factura asociada a un pedido.
     * @param request DTO con los datos del pedido
     * @return El DTO de la factura creada
     */
    FacturaResponse crearFactura(CrearFacturaRequest request);

    /**
     * Busca una factura por su ID.
     * @param id El ID de la factura
     * @return El DTO de la factura
     */
    FacturaResponse obtenerFacturaPorId(UUID id);


    /**
     * Lista todas las facturas con un estado específico.
     * @param estado Estado a filtrar (PENDIENTE, PAGADA, ANULADA)
     * @return Lista de DTOs de facturas
     */
    List<FacturaResponse> listarFacturasPorEstado(EstadoFactura estado);

    /**
     * Anula una factura (ej: si el pedido se cancela).
     * @param id El ID de la factura a anular
     * @return El DTO de la factura actualizada (en estado ANULADA)
     */
    FacturaResponse anularFactura(UUID id);


}