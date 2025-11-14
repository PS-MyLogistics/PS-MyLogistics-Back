package com.mylogisticcba.core.mercadopago.impl;

import com.mylogisticcba.core.mercadopago.entity.Payment;
import com.mylogisticcba.core.mercadopago.repository.PaymentRepository;
import com.mylogisticcba.core.payments.mercadopago.MercadoPagoConfig;
import com.mylogisticcba.core.payments.mercadopago.MercadoPagoService;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentRequest;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentResponse;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentGetRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.Item;
import com.mercadopago.resources.datastructures.preference.Payer;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final MercadoPagoConfig config;
    private final PaymentRepository paymentRepository;

    @Autowired
    public MercadoPagoServiceImpl(MercadoPagoConfig config, PaymentRepository paymentRepository) {
        this.config = config;
        this.paymentRepository = paymentRepository;
        try {
            MercadoPago.SDK.setAccessToken(config.getAccessToken());
        } catch (MPException e) {
            throw new RuntimeException("Error initializing MercadoPago SDK", e);
        }
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest req) throws Exception {
        PreferenceClient preferenceClient = new PreferenceClient();
        PreferenceRequest preferenceRequest = new PreferenceRequest();

        Item item = new Item()
                .setTitle(Optional.ofNullable(req.getDescription()).orElse("Pago"))
                .setQuantity(1)
                .setUnitPrice(req.getAmount())
                .setCurrencyId(Optional.ofNullable(req.getCurrency()).orElse("ARS"));

        preferenceRequest.appendItem(item);

        if (req.getPayerEmail() != null) {
            Payer payer = new Payer();
            payer.setEmail(req.getPayerEmail());
            preferenceRequest.setPayer(payer);
        }

        if (req.getExternalReference() != null) {
            preferenceRequest.setExternalReference(req.getExternalReference());
        }

        Preference savedPreference = preferenceClient.create(preferenceRequest);

        Payment paymentEntity = new Payment();
        paymentEntity.setId(savedPreference.getId());
        paymentEntity.setStatus("created");
        paymentEntity.setTransactionAmount(req.getAmount());
        paymentEntity.setCurrency(req.getCurrency());
        paymentEntity.setExternalReference(req.getExternalReference());
        paymentEntity.setCreatedAt(LocalDateTime.now());
        paymentEntity.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(paymentEntity);

        PaymentResponse response = new PaymentResponse();
        response.setId(savedPreference.getId());
        response.setInitPoint(savedPreference.getInitPoint());
        response.setStatus("created");
        response.setTransactionAmount(req.getAmount());
        response.setCurrency(req.getCurrency());
        response.setExternalReference(req.getExternalReference());

        return response;
    }

    @Override
    public PaymentResponse getPayment(String paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId).get();

        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setStatus(payment.getStatus());
        response.setTransactionAmount(payment.getTransactionAmount());
        response.setCurrency(payment.getCurrency());
        response.setExternalReference(payment.getExternalReference());

        com.mylogisticcba.core.mercadopago.entity.Payment paymentEntity = new com.mylogisticcba.core.mercadopago.entity.Payment();
        paymentEntity.setId(payment.getId());
        paymentEntity.setStatus(payment.getStatus());
        paymentEntity.setTransactionAmount(payment.getTransactionAmount());
        paymentEntity.setCurrency(payment.getCurrency());
        paymentEntity.setExternalReference(payment.getExternalReference());
        paymentEntity.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(paymentEntity);

        return response;
    }
}
