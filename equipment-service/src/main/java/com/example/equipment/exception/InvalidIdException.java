package com.example.equipment.exception;

public class InvalidIdException extends RuntimeException {
    public InvalidIdException(String fieldName) {
        super(fieldName + " is not a valid");
    }
}
