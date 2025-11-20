package com.mylogisticcba.payments.mercadoPago.event;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class PaymentCreditedEvent {

    private UUID tenantId;
    private UUID facturaId;
    private UUID owner;
    private UUID cliente;
    private LocalDateTime paymentAt; // fecha/hora del pago en el momento en que se procesó
    private Integer months; // opcional: duración en meses (1,3,6) que solicita el evento

}
