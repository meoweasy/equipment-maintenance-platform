package com.example.platform.common.exception;

public class InvalidFieldValueException extends RuntimeException {
    public InvalidFieldValueException(String fieldName) {
        super(fieldName + " is not valid");
    }
}
