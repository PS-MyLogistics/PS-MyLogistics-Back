package com.mylogisticcba.payments.mercadoPago.exception;

import com.mylogisticcba.payments.mercadoPago.dto.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;


@RestControllerAdvice( basePackages = "com.mylogisticcba.core.payments.mercadoPago.controller")
public class MercadoPagoGlobalExceptionHandler {

    /**
     * Maneja nuestra excepción personalizada FacturaNotFoundException.
     */
    @ExceptionHandler(com.mylogisticcba.payments.mercadoPago.exception.FacturaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFacturaNotFound(FacturaNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja la excepción 'IllegalStateException' que usamos en anularFactura.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), // 400 Bad Request
                "Operación no válida",
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Manejador genérico para cualquier otra excepción no controlada.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ocurrió un error inesperado. Por favor, contacte al soporte."
        );
        // Es importante loguear el error real para debugging
        // log.error("Excepción no controlada: ", ex);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}