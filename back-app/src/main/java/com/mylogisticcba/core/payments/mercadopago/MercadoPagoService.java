package com.mylogisticcba.core.payments.mercadopago;

import com.mylogisticcba.core.payments.mercadopago.dto.PaymentRequest;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentResponse;

public interface MercadoPagoService {
    PaymentResponse createPayment(PaymentRequest req) throws Exception;
    PaymentResponse getPayment(String paymentId) throws Exception;
}

