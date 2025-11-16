package com.mylogisticcba.payments.mercadoPago.repository;


import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, UUID> {
 
    /**
     * Busca una factura por su ID de pedido (vínculo lógico).
     * @param pedidoId El ID del pedido en 'pedido-service'
     * @return Una Factura (opcional)
     */

    /**
     * Busca todas las facturas de un cliente por su ID.
     * @param clienteId El ID del cliente en 'clientes-service'
     * @return Lista de facturas
     */
    List<Factura> findByClienteId(UUID clienteId);

    /**
     * Busca todas las facturas que tengan un estado específico.
     * Útil para buscar, por ejemplo, todas las facturas PENDIENTES.
     * @param estado El EstadoFactura (PENDIENTE, PAGADA, ANULADA)
     * @return Lista de facturas
     */
    List<Factura> findByEstado(EstadoFactura estado);
}