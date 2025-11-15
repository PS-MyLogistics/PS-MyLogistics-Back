package com.mylogisticcba.notification.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice(basePackages = "com.mylogisticcba.notification")
public class NotificationExceptionHandler {


        @ExceptionHandler(NotificationException.class)
        public ResponseEntity<String> handleNotificationException(NotificationException ex) {
            log.warn("Error controlado Notificaciones: {}", ex.getMessage());
            return ResponseEntity
                    .status(ex.getStatus())
                    .body(ex.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<String> handleGeneralException(Exception ex) {
            log.error("Error inesperado Notificaicones: {}", ex.getMessage());
            return ResponseEntity
                    .status(500)
                    .body("Ocurrió un error interno , por favor intenta más tarde.");
        }

    }


