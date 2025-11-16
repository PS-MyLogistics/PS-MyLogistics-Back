package com.mylogisticcba.iam.tenant.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.mylogisticcba.iam.tenant")
public class TenantExceptionHandler {


    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity< Map<String, Object>> handleTenantException(UserServiceException ex, HttpServletRequest request) {
        log.warn("Error controlado en iam.tenant => user service: {}", ex.getMessage());

        // Json Standard to facilicitate frontend
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("message",ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(ex.getStatus())
                .body(body);
    }
    @ExceptionHandler(TenantServiceException.class)
    public ResponseEntity< Map<String, Object>> handleTenantException(TenantServiceException ex,HttpServletRequest request) {
        log.warn("Error controlado en iam.tenant => tenant service: {}", ex.getMessage());

        // Json Standard to facilicitate frontend
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", ex.getStatus().value());
        body.put("error", ex.getStatus().getReasonPhrase());
        body.put("message",ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(ex.getStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity< Map<String, Object>> handleGeneralException(Exception ex,HttpServletRequest request) {
        log.error("Error NO controlado en iam.tenant: {}", ex.getMessage());
        // Json Standard to facilicitate frontend
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "Ocurrió un error interno, por favor intentar más tarde.");
        body.put("path", request.getRequestURI());
        return ResponseEntity
                .status(500)
                .body(body);
    }
}
