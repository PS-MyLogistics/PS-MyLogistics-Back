package com.mylogisticcba.iam.security.auth.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.mylogisticcba.iam.security.auth")
public class AuthExceptionHandler {

    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthServiceException ex, HttpServletRequest request) {
        log.warn("AuthServiceE", ex);
        log.warn(",Error controlled in AuthService: AuthServiceException  {}", ex.getMessage());
        Map<String, Object> body = jsonResponseBuilder(HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication failed",
                request.getRequestURI());
        return ResponseEntity
                .status(ex.getStatus())
                .body(body);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Error controlled in AuthService: AuthenticationException {}", ex.getMessage());
        Map<String, Object> body = jsonResponseBuilder(HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication failed",
                request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED.value())
                .body(body);
    }
    @ExceptionHandler(ResetPasswordServiceException.class)
    public ResponseEntity<Map<String, Object>> handleResetPassException(ResetPasswordServiceException ex, HttpServletRequest request) {
        log.warn("Error controlled in ResetPasswordService  : ResetPasswordServiceException  {}", ex.getMessage());
        // Json Standard to facilicitate frontend
        Map<String, Object> body = jsonResponseBuilder(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication failed",
                request.getRequestURI());
        return ResponseEntity
                .status(ex.getStatus())
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex , HttpServletRequest request) {
        log.error("Error Not controlled in Auth: Exception  {}", ex.getMessage());
        // Json Standard to facilicitate frontend
        Map<String, Object> body = jsonResponseBuilder(
                500,
                "Internal Server Error",
                "Ocurrió un error interno, por favor intentar más tarde.",
                request.getRequestURI()
        );
        return ResponseEntity
                .status(500)
                .body(body);
    }

    private Map<String, Object> jsonResponseBuilder(int status,String errorName,String message,String path) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status);
        body.put("error", errorName);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
