package com.example.platform.common.exception;

public class InvalidIdException extends RuntimeException {
    public InvalidIdException(String fieldName) {
        super(fieldName + " is not a valid");
    }
}
