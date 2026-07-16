package com.example.equipment.exception;

public class ValueTooLargeException extends RuntimeException {
    public ValueTooLargeException(String fieldName, Number maxValue) {
        super(fieldName + " must not be greater than " + maxValue);
    }
}
