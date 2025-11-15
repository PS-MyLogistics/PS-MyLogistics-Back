package com.mylogisticcba.iam.security.auth.exceptions;

import org.springframework.http.HttpStatus;


public class AuthServiceException extends RuntimeException {
    private final HttpStatus status;


    public AuthServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public AuthServiceException(String message,HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    }

}