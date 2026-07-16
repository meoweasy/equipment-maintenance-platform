package com.example.equipment.exception;

public class ValueTooSmallException extends RuntimeException {
    public ValueTooSmallException(String fieldName, Number minValue) {
        super(fieldName + " must not be less than " + minValue);
    }
}
