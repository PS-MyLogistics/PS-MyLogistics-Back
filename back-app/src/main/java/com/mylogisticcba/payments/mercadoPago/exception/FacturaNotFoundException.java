package com.mylogisticcba.payments.mercadoPago.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Esta anotación le dice a Spring que si esta excepción
// no es capturada, debe devolver un 404 Not Found
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FacturaNotFoundException extends RuntimeException {
    
    public FacturaNotFoundException(String message) {
        super(message);
    }
}