package com.mylogisticcba.payments.mercadoPago.service.impl;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;
import com.mylogisticcba.iam.security.auth.securityCustoms.TenantContextHolder;
import com.mylogisticcba.iam.tenant.services.impl.TenantService;
import com.mylogisticcba.payments.mercadoPago.dto.*;
import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import com.mylogisticcba.payments.mercadoPago.entity.Pago;
import com.mylogisticcba.payments.mercadoPago.exception.FacturaNotFoundException;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoPago;
import com.mylogisticcba.payments.mercadoPago.repository.FacturaRepository;
import com.mylogisticcba.payments.mercadoPago.repository.PagoRepository;
import com.mylogisticcba.payments.mercadoPago.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final TenantService tenantService;
    private final FacturaServiceImpl facturaService;

    @Override
    @Transactional
    public PagoResponse registrarPago(RegistrarPagoRequest request) {
        log.info("Iniciando registro de pago para factura ID: {}", request.getFacturaId());

        // 1. Validaciones
        log.info("registrarPago - facturaId recibido: {}", request.getFacturaId());
        boolean exists = false;
        try {
            exists = facturaRepository.existsById(request.getFacturaId());
        } catch (Exception e) {
            log.error("Error comprobando existencia de factura: {}", e.getMessage(), e);
        }
        log.info("registrarPago - factura existsById: {}", exists);

        var optFactura = facturaRepository.findById(request.getFacturaId());
        log.info("registrarPago - findById presente: {}", optFactura.isPresent());
        if (optFactura.isEmpty()) {
            throw new FacturaNotFoundException("Factura no encontrada con ID: " + request.getFacturaId());
        }
        Factura factura = optFactura.get();
        log.info("registrarPago - factura cargada: id={}, tenantId={}", factura.getId(), factura.getTenantId());

        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new IllegalStateException("Esta factura ya fue pagada.");
        }
        if (factura.getEstado() == EstadoFactura.ANULADA) {
            throw new IllegalStateException("Esta factura está anulada.");
        }

        var tenant = tenantService.getTenantById(factura.getTenantId());
        if (!tenant.getId().equals(TenantContextHolder.getTenant())) {
            throw new RuntimeException("El tenant de la factura no coincide con el contexto actual");
        }

        //TODO: guardad en el monto del pago al monto guardado en la preference
        // 2. Crear pago en estado PENDIENTE
        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(factura.getTotal().doubleValue());
        pago.setEstadoPago(EstadoPago.PENDIENTE);
        Pago pagoEnDB = pagoRepository.save(pago);

        log.info("Pago ID: {} creado en estado PENDIENTE.", pagoEnDB.getId());

        try {
            // 3. Crear el ítem
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id("item-" + pagoEnDB.getId())
                    .title("Subscripcion MyLogisticCBA por " + request.getMeses() + " meses")
                    .description("Pago de Subscripcion mensual a MyLogisticCBA x"+ request.getMeses())
                    .quantity(request.getMeses())
                    .currencyId("ARS")
                    //agrega el precio unitario como 1 para que el total sea igual a la cantidad de meses
                    .unitPrice(BigDecimal.valueOf(1.00))
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // 4. Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:8081/pagos/success?pagoId=" + pagoEnDB.getId())
                    .failure("http://localhost:8081/pagos/failure?pagoId=" + pagoEnDB.getId())
                    .pending("http://localhost:8081/pagos/pending?pagoId=" + pagoEnDB.getId())
                    .build();

            // 5. Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    // No seteamos autoReturn aquí para evitar validación estricta del gateway
                    .externalReference(pagoEnDB.getId().toString()) // ⬅️ CRÍTICO para el webhook
                    // Para pruebas locales usar http y evitar problemas con certificados
                    .notificationUrl("http://localhost:8081/api/webhooks/mercadopago")
                    .build();

            PreferenceClient client = new PreferenceClient();
            log.info("PreferenceRequest back_urls.success={}", backUrls != null ? "defined" : "null");
            log.info("PreferenceRequest external_reference={}", pagoEnDB.getId());
            Preference preference = client.create(preferenceRequest);

            // 6. Guardar el ID de la preferencia
            pagoEnDB.setIdPreference(preference.getId());
            pagoRepository.save(pagoEnDB);

            log.info("✅ Preferencia creada en Mercado Pago");
            log.info("   Preference ID: {}", preference.getId());
            log.info("   Init Point: {}", preference.getInitPoint());

            // 7. Devolver respuesta
            return PagoResponse.builder()
                    .id(pagoEnDB.getId())
                    .monto(pagoEnDB.getMonto())
                    .estadoPago(pagoEnDB.getEstadoPago())
                    .transaccionIdExterno(pagoEnDB.getTransaccionIdExterno())
                    .facturaId(factura.getId())
                    .preferenciaPagoId(preference.getId())
                    .initPoint(preference.getInitPoint()) // ⬅️ URL para redirigir al usuario
                    .build();

        } catch (MPApiException mpe) {
            // Loguear detalles de la respuesta de la API para diagnóstico
            log.error("❌ MPApiException al crear preferencia. Status: {}", mpe.getStatusCode());
            if (mpe.getApiResponse() != null) {
                try {
                    log.error("❌ MPApiException response body: {}", mpe.getApiResponse().getContent());
                } catch (Exception e) {
                    log.error("❌ Error al leer contenido de MPApiException", e);
                }
            }
            pagoEnDB.setEstadoPago(EstadoPago.FALLIDO);
            pagoRepository.save(pagoEnDB);
            throw new RuntimeException("Error de API de Mercado Pago al crear preferencia", mpe);
        } catch (Exception ex) {
            log.error("❌ Error al crear preferencia", ex);
            pagoEnDB.setEstadoPago(EstadoPago.FALLIDO);
            pagoRepository.save(pagoEnDB);
            throw new RuntimeException("Error al crear preferencia de pago", ex);
        }
    }


}