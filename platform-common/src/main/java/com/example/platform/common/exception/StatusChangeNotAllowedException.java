package com.example.platform.common.exception;

public class StatusChangeNotAllowedException extends RuntimeException {
    public StatusChangeNotAllowedException(String message) {
        super(message);
    }
}
