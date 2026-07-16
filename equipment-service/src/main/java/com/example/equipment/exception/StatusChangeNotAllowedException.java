package com.example.equipment.exception;

public class StatusChangeNotAllowedException extends RuntimeException {
    public StatusChangeNotAllowedException(String message) {
        super(message);
    }
}
