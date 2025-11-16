package com.mylogisticcba.payments.mercadoPago.service.impl;

import com.mylogisticcba.payments.mercadoPago.dto.CrearFacturaRequest;
import com.mylogisticcba.payments.mercadoPago.dto.FacturaResponse;
import com.mylogisticcba.payments.mercadoPago.dto.mapper.FacturaMapper;
import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import com.mylogisticcba.payments.mercadoPago.exception.FacturaNotFoundException;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import com.mylogisticcba.payments.mercadoPago.repository.FacturaRepository;
import com.mylogisticcba.payments.mercadoPago.service.FacturaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FacturaServiceImpl implements FacturaService {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;

    // Inyección de dependencias por constructor (la mejor práctica)
    public FacturaServiceImpl(FacturaRepository facturaRepository, FacturaMapper facturaMapper) {
        this.facturaRepository = facturaRepository;
        this.facturaMapper = facturaMapper;
    }

    @Override
    @Transactional
    public FacturaResponse crearFactura(CrearFacturaRequest request) {
        log.info("Creando factura para pedidoId: {}", request.getTenantId());


        Factura factura = facturaMapper.toEntity(request);

        // 2. Aplicar lógica de negocio
        // Generamos un número de factura único. UUID es simple y efectivo.
        factura.setNumeroFactura("F-" + UUID.randomUUID().toString().substring(0, 13));
        // El estado PENDIENTE ya lo asigna el mapper.

        // 3. Guardar en BBDD
        Factura facturaGuardada = facturaRepository.save(factura);
        log.info("Factura creada con ID: {}", facturaGuardada.getId());

        // 4. Convertir Entidad a DTO de respuesta y devolver
        return facturaMapper.toResponse(facturaGuardada);
    }

    @Override
    @Transactional(readOnly = true) // Optimizada para consultas de solo lectura
    public FacturaResponse obtenerFacturaPorId(UUID id) {
        log.info("Buscando factura con ID: {}", id);
        Factura factura = findFacturaById(id); // Usa nuestro método privado
        return facturaMapper.toResponse(factura);
    }


    @Override
    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasPorEstado(EstadoFactura estado) {
        log.info("Listando facturas con estado: {}", estado);
        List<Factura> facturas = facturaRepository.findByEstado(estado);
        // Usamos el método del mapper para convertir la lista
        return facturaMapper.toFacturaResponseList(facturas);
    }

    @Override
    @Transactional
    public FacturaResponse anularFactura(UUID id) {
        log.info("Anulando factura con ID: {}", id);
        Factura factura = findFacturaById(id);

        // Lógica de negocio: ¿se puede anular?
        if (factura.getEstado() == EstadoFactura.PAGADA) {
            // No se debería anular una factura pagada (requeriría un Reembolso/Nota de Crédito)
            log.warn("Intento de anular factura PAGADA (ID: {}). Operación denegada.", id);
            throw new IllegalStateException("No se puede anular una factura que ya ha sido pagada.");
        }

        factura.setEstado(EstadoFactura.ANULADA);
        Factura facturaAnulada = facturaRepository.save(factura);
        
        return facturaMapper.toResponse(facturaAnulada);
    }
    
    // --- Método Privado de Ayuda (Helper Method) ---
    
    /**
     * Método centralizado para buscar una factura por ID.
     * Si no la encuentra, lanza la excepción estándar del servicio.
     * @param id El ID de la Factura
     * @return La Entidad Factura
     */
    private Factura findFacturaById(UUID id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new FacturaNotFoundException("Factura no encontrada con ID: " + id));
    }
}