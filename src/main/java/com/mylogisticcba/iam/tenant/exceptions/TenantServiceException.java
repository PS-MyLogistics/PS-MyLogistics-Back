package com.mylogisticcba.iam.tenant.exceptions;

import org.springframework.http.HttpStatus;

public class TenantServiceException extends RuntimeException {
    private final HttpStatus status;


    public TenantServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public TenantServiceException(String message,HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    }}
