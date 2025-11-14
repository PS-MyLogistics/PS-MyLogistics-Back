package com.mylogisticcba.core.payments.mercadopago.controller;

import com.mylogisticcba.core.payments.mercadopago.MercadoPagoService;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentRequest;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @Autowired
    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest req) {
        try {
            PaymentResponse resp = mercadoPagoService.createPayment(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<?> getPayment(@PathVariable("id") String id) {
        try {
            PaymentResponse resp = mercadoPagoService.getPayment(id);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

