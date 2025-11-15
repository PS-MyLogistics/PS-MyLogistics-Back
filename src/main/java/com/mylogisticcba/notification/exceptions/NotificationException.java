package com.mylogisticcba.notification.exceptions;


import org.springframework.http.HttpStatus;

public class NotificationException extends RuntimeException {

    private final HttpStatus status;

    public NotificationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public NotificationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
    public HttpStatus getStatus() {
        return status;
    }
}