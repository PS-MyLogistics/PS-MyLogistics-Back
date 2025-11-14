package com.mylogisticcba.core.mercadopago.controller;

import com.mylogisticcba.core.mercadopago.entity.Payment;
import com.mylogisticcba.core.mercadopago.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago/webhook")
public class WebhookController {

    private final PaymentRepository paymentRepository;

    @Autowired
    public WebhookController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String paymentId = (String) payload.get("id");
            String status = (String) payload.get("status");

            Payment payment = paymentRepository.findById(paymentId).orElse(new Payment());
            payment.setId(paymentId);
            payment.setStatus(status);
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook: " + e.getMessage());
        }
    }
}
