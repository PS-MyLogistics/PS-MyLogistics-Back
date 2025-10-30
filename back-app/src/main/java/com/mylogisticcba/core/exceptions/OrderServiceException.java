package com.mylogisticcba.core.exceptions;

import org.springframework.http.HttpStatus;

public class OrderServiceException extends RuntimeException {
    private final HttpStatus status;

    public OrderServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public OrderServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

