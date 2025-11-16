package com.mylogisticcba.payments.mercadoPago.controller;

import com.mylogisticcba.payments.mercadoPago.service.impl.WebHookServiceMP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador que recibe las notificaciones (webhooks) de Mercado Pago
 *
 * Cuando un pago cambia de estado (aprobado, rechazado, etc.),
 * Mercado Pago envía un POST a esta URL con la información del pago.
 */
@RestController
@RequestMapping("/api/webhooks/mercadopago")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebHookServiceMP webhookService;

    /**
     * Endpoint principal que recibe las notificaciones de Mercado Pago
     *
     * Mercado Pago envía requests en este formato:
     * POST /api/webhooks/mercadopago?topic=payment&id=123456789
     *
     * @param topic Tipo de notificación: "payment" o "merchant_order"
     * @param id ID del recurso (payment ID o merchant_order ID)
     * @param body Cuerpo de la request (opcional, generalmente vacío)
     * @return 200 OK siempre (para que MP no reintente)
     */
    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {

        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📩 WEBHOOK RECIBIDO DE MERCADO PAGO");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("   Topic: {}", topic);
        log.info("   ID: {}", id);
        log.info("   Body: {}", body);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // Validar que vengan los parámetros necesarios
            if (topic == null || id == null) {
                log.warn("⚠️ Webhook inválido: falta topic o id");
                return ResponseEntity.ok().build();
            }

            // Procesar según el tipo de notificación
            if ("payment".equals(topic)) {
                log.info("🔄 Procesando notificación de PAYMENT");
                webhookService.procesarPagoWebhook(id);

            } else if ("merchant_order".equals(topic)) {
                log.info("🔄 Procesando notificación de MERCHANT_ORDER");
                webhookService.procesarOrdenWebhook(id);

            } else {
                log.warn("⚠️ Topic desconocido: {}", topic);
            }

            // CRÍTICO: Siempre responder 200 OK rápidamente
            // Si respondes error, Mercado Pago reintentará la notificación
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("❌ Error procesando webhook", e);

            // Aún con error, devolver 200 OK para evitar reintentos
            // (ya logueamos el error para debugging)
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Endpoint para testing manual del webhook
     *
     * Útil para probar el flujo sin tener que hacer un pago real.
     *
     * Ejemplo de uso:
     * GET /api/webhooks/mercadopago/test?paymentId=123456789
     *
     * @param paymentId ID del pago en Mercado Pago
     * @return Resultado del procesamiento
     */
    @GetMapping("/test")
    public ResponseEntity<String> testWebhook(@RequestParam String paymentId) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("🧪 TEST MANUAL DEL WEBHOOK");
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("   Payment ID: {}", paymentId);
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            webhookService.procesarPagoWebhook(paymentId);
            return ResponseEntity.ok("✅ Webhook procesado correctamente para payment ID: " + paymentId);

        } catch (Exception e) {
            log.error("❌ Error en test de webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Endpoint para verificar que el webhook está activo
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ Webhook endpoint is active");
    }
}