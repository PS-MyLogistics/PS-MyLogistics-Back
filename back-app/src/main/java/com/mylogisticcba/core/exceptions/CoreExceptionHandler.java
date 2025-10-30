package com.mylogisticcba.core.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.mylogisticcba.core")
public class CoreExceptionHandler {

    @ExceptionHandler(OrderServiceException.class)
    public ResponseEntity<Map<String, Object>> handleOrderServiceException(OrderServiceException ex, HttpServletRequest request) {
        log.warn("Error controlado en core => order service: {}", ex.getMessage());

        Map<String, Object> body = jsonResponseBuilder(
                ex.getStatus().value(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Error NO controlado en core: {}", ex.getMessage());

        Map<String, Object> body = jsonResponseBuilder(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrió un error interno, por favor intentar más tarde.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    private Map<String, Object> jsonResponseBuilder(int status, String errorName, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status);
        body.put("error", errorName);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}

