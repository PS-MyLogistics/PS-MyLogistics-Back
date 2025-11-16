package com.mylogisticcba.payments.mercadoPago.repository;


import com.mylogisticcba.payments.mercadoPago.entity.Pago;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PagoRepository extends JpaRepository<Pago, UUID> {
   
    /**
     * Busca todos los pagos asociados a un ID de factura.
     * @param facturaId El ID de la Factura (PK)
     * @return Lista de pagos
     */
    List<Pago> findByFacturaId(UUID facturaId);

    /**
     * Busca todos los pagos que tengan un estado específico.
     * @param estado El EstadoPago (PENDIENTE, EXITOSO, FALLIDO)
     * @return Lista de pagos
     */
    List<Pago> findByEstadoPago(EstadoPago estado);
}