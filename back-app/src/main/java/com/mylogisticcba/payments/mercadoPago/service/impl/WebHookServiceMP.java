package com.mylogisticcba.payments.mercadoPago.service.impl;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mylogisticcba.iam.tenant.entity.TenantEntity;
import com.mylogisticcba.iam.tenant.services.impl.TenantService;
import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import com.mylogisticcba.payments.mercadoPago.entity.Pago;
import com.mylogisticcba.payments.mercadoPago.event.PaymentCreditedEvent;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoFactura;
import com.mylogisticcba.payments.mercadoPago.model.enums.EstadoPago;
import com.mylogisticcba.payments.mercadoPago.repository.FacturaRepository;
import com.mylogisticcba.payments.mercadoPago.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebHookServiceMP {

    private final PagoRepository pagoRepository;
    private final FacturaRepository facturaRepository;
    private final ApplicationEventPublisher publisher;
    private final TenantService tenantService;

    /**
     * Procesa la notificación de un pago de Mercado Pago
     * @param paymentId ID del pago en Mercado Pago (NO es tu Pago ID)
     */
    @Transactional
    public void procesarPagoWebhook(String paymentId) {
        log.info("🔄 Procesando webhook de pago. Payment ID de MP: {}", paymentId);

        try {
            // 1. Consultar el pago en Mercado Pago usando el SDK
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));

            log.info("💳 Información del pago obtenida de Mercado Pago:");
            log.info("   Payment ID (MP): {}", payment.getId());
            log.info("   Status: {}", payment.getStatus());
            log.info("   Status Detail: {}", payment.getStatusDetail());
            log.info("   External Reference: {}", payment.getExternalReference());
            log.info("   Transaction Amount: {}", payment.getTransactionAmount());
            log.info("   Payment Method: {}", payment.getPaymentMethodId());

            // 2. Validar que tenga external_reference
            if (payment.getExternalReference() == null || payment.getExternalReference().isEmpty()) {
                log.warn("⚠️ El pago no tiene external_reference. No se puede procesar.");
                log.warn("   Asegúrate de configurar .externalReference() al crear la preferencia");
                return;
            }

            // 3. Buscar el pago en nuestra base de datos usando el external_reference
            UUID pagoId = UUID.fromString(payment.getExternalReference());
            Pago pago = pagoRepository.findById(pagoId)
                    .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));

            log.info("📦 Pago encontrado en nuestra DB:");
            log.info("   Pago ID: {}", pago.getId());
            log.info("   Estado actual: {}", pago.getEstadoPago());
            log.info("   Monto: {}", pago.getMonto());

            // 4. Actualizar el estado del pago según la respuesta de Mercado Pago
            actualizarEstadoPago(pago, payment);

            log.info("✅ Webhook procesado exitosamente para Pago ID: {}", pago.getId());

        } catch (MPApiException e) {
            log.error("❌ Error de API de Mercado Pago al consultar payment {}", paymentId);
            log.error("   Status Code: {}", e.getStatusCode());
            log.error("   Response: {}", e.getApiResponse().getContent());
            throw new RuntimeException("Error consultando pago en Mercado Pago", e);

        } catch (MPException e) {
            log.error("❌ Error del SDK de Mercado Pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error del SDK de Mercado Pago", e);

        } catch (NumberFormatException e) {
            log.error("❌ Error: payment ID o external_reference no es un número válido", e);
            throw new RuntimeException("ID de pago inválido", e);

        } catch (Exception e) {
            log.error("❌ Error inesperado procesando webhook", e);
            throw new RuntimeException("Error procesando webhook", e);
        }
    }

    /**
     * Procesa la notificación de una orden de mercado (merchant_order)
     * Esto se puede usar para pagos más complejos con múltiples items
     */
    @Transactional
    public void procesarOrdenWebhook(String orderId) {
        log.info("🔄 Procesando webhook de orden: {}", orderId);
        // TODO: Implementar según necesidad
        log.warn("⚠️ Procesamiento de merchant_order no implementado");
    }

    /**
     * Actualiza el estado del pago en nuestra DB según el estado de Mercado Pago
     */
    private void actualizarEstadoPago(Pago pago, Payment payment) {
        Factura factura = pago.getFactura();
        EstadoPago estadoAnterior = pago.getEstadoPago();

        log.info("🔄 Actualizando estado del pago...");
        log.info("   Estado anterior: {}", estadoAnterior);
        log.info("   Nuevo estado de MP: {}", payment.getStatus());

        switch (payment.getStatus()) {
            case "approved":
                log.info("✅ Pago APROBADO");

                // Actualizar el pago
                pago.setEstadoPago(EstadoPago.EXITOSO);
                pago.setTransaccionIdExterno(payment.getId().toString());

                // Actualizar la factura
                factura.setEstado(EstadoFactura.PAGADA);
                facturaRepository.save(factura);

                // Publicar evento solo si cambió el estado
                publicarEventoPagoExitoso(factura);

                break;

            case "rejected":
                log.warn("❌ Pago RECHAZADO");
                log.warn("   Razón: {}", payment.getStatusDetail());
                pago.setEstadoPago(EstadoPago.FALLIDO);
                pago.setTransaccionIdExterno(payment.getId().toString());
                break;

            case "pending":
                log.info("⏳ Pago EN PROCESO (pending)");
                pago.setEstadoPago(EstadoPago.PENDIENTE);
                pago.setTransaccionIdExterno(payment.getId().toString());
                break;

            case "in_process":
                log.info("⏳ Pago EN PROCESO (in_process)");
                pago.setEstadoPago(EstadoPago.PENDIENTE);
                pago.setTransaccionIdExterno(payment.getId().toString());
                break;

            case "in_mediation":
                log.warn("⚖️ Pago EN MEDIACIÓN");
                pago.setEstadoPago(EstadoPago.PENDIENTE);
                pago.setTransaccionIdExterno(payment.getId().toString());
                break;

            case "cancelled":
                log.warn("🚫 Pago CANCELADO");
                pago.setEstadoPago(EstadoPago.FALLIDO);
                pago.setTransaccionIdExterno(payment.getId().toString());
                break;

            case "refunded":
                log.warn("🔙 Pago REEMBOLSADO");
                pago.setEstadoPago(EstadoPago.FALLIDO);
                pago.setTransaccionIdExterno(payment.getId().toString());

                // Si la factura estaba pagada, revertir
                if (factura.getEstado() == EstadoFactura.PAGADA) {
                    factura.setEstado(EstadoFactura.PENDIENTE);
                    facturaRepository.save(factura);
                }
                break;

            case "charged_back":
                log.warn("⚠️ Pago con CONTRACARGO");
                pago.setEstadoPago(EstadoPago.FALLIDO);
                pago.setTransaccionIdExterno(payment.getId().toString());

                if (factura.getEstado() == EstadoFactura.PAGADA) {
                    factura.setEstado(EstadoFactura.PENDIENTE);
                    facturaRepository.save(factura);
                }
                break;

            default:
                log.warn("⚠️ Estado de pago desconocido: {}", payment.getStatus());
                pago.setTransaccionIdExterno(payment.getId().toString());
        }

        // Guardar cambios
        pagoRepository.save(pago);
        log.info("💾 Pago actualizado en DB. Nuevo estado: {}", pago.getEstadoPago());
    }

    /**
     * Publica el evento de pago exitoso para notificar a otros módulos
     */
    private void publicarEventoPagoExitoso(Factura factura) {
        try {
            TenantEntity tenant = tenantService.getTenantById(factura.getTenantId());

            PaymentCreditedEvent event = new PaymentCreditedEvent();
            event.setCliente(factura.getClienteId());
            event.setFacturaId(factura.getId());
            event.setOwner(tenant.getOwnerId());
            event.setTenantId(tenant.getId());

            // Publicar AFTER COMMIT para que listeners vean datos ya persistidos
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            publisher.publishEvent(event);
                            log.info("📢 Evento PaymentCreditedEvent publicado (afterCommit)");
                        } catch (Exception ex) {
                            log.error("❌ Error publicando evento después del commit", ex);
                        }
                    }
                });
            } else {
                publisher.publishEvent(event);
                log.info("📢 Evento PaymentCreditedEvent publicado");
            }

            log.info("   Cliente: {}", factura.getClienteId());
            log.info("   Factura: {}", factura.getId());
            log.info("   Tenant: {}", tenant.getId());

        } catch (Exception e) {
            log.error("❌ Error al publicar evento de pago exitoso", e);
            // No lanzamos excepción para no afectar el flujo principal
        }
    }
}