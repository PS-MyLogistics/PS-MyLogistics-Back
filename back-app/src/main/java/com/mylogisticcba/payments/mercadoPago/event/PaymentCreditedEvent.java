package com.mylogisticcba.payments.mercadoPago.event;

import com.mylogisticcba.payments.mercadoPago.entity.Factura;
import lombok.Data;

import java.util.UUID;
@Data
public class PaymentCreditedEvent {

    private UUID tenantId;
    private UUID facturaId;
    private UUID owner;
    private UUID cliente;

}
