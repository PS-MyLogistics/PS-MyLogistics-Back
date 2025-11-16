package com.mylogisticcba.iam.security.auth.exceptions;

import org.springframework.http.HttpStatus;

public class ResetPasswordServiceException extends RuntimeException {
    private final HttpStatus status;


    public ResetPasswordServiceException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ResetPasswordServiceException(String message,HttpStatus status) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    }

}
