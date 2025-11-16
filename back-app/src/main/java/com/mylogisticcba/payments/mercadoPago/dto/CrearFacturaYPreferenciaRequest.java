package com.mylogisticcba.payments.mercadoPago.dto;

import lombok.Data;

@Data
public class CrearFacturaYPreferenciaRequest {
    CrearFacturaRequest requestFactura;
    RegistrarPagoRequest requestPago;

}
