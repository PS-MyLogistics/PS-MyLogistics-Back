package com.mylogisticcba.core.payments.mercadopago.impl;

import com.mylogisticcba.core.payments.mercadopago.MercadoPagoConfig;
import com.mylogisticcba.core.payments.mercadopago.MercadoPagoService;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentRequest;
import com.mylogisticcba.core.payments.mercadopago.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final MercadoPagoConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public MercadoPagoServiceImpl(MercadoPagoConfig config) {
        this.config = config;
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest req) throws Exception {
        String url = config.getBaseUrl() + "/checkout/preferences";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getAccessToken());

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", req.getDescription() == null ? "Pago" : req.getDescription());
        item.put("quantity", 1);
        item.put("unit_price", req.getAmount());
        item.put("currency_id", req.getCurrency() == null ? "ARS" : req.getCurrency());
        body.put("items", List.of(item));
        if (req.getExternalReference() != null) {
            body.put("external_reference", req.getExternalReference());
        }
        Map<String, Object> payer = new HashMap<>();
        if (req.getPayerEmail() != null) {
            payer.put("email", req.getPayerEmail());
            body.put("payer", payer);
        }

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> respBody = resp.getBody();
        PaymentResponse out = new PaymentResponse();
        if (respBody != null) {
            Object id = respBody.get("id");
            Object initPoint = respBody.get("init_point");
            out.setId(id == null ? null : id.toString());
            out.setInitPoint(initPoint == null ? null : initPoint.toString());
            out.setStatus((String) respBody.get("status"));
            out.setTransactionAmount(req.getAmount());
            out.setCurrency(req.getCurrency());
            out.setExternalReference(req.getExternalReference());
        }

        return out;
    }

    @Override
    public PaymentResponse getPayment(String paymentId) throws Exception {
        String url = config.getBaseUrl() + "/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getAccessToken());
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> respBody = resp.getBody();
        PaymentResponse out = new PaymentResponse();
        if (respBody != null) {
            out.setId(respBody.get("id") == null ? null : respBody.get("id").toString());
            out.setStatus(respBody.get("status") == null ? null : respBody.get("status").toString());
            // amount and currency may be nested
            Object transactionAmount = respBody.get("transaction_amount");
            if (transactionAmount instanceof Number) {
                out.setTransactionAmount(((Number) transactionAmount).doubleValue());
            }
            out.setCurrency(respBody.get("currency_id") == null ? null : respBody.get("currency_id").toString());
            out.setExternalReference(respBody.get("external_reference") == null ? null : respBody.get("external_reference").toString());
        }

        return out;
    }
}

