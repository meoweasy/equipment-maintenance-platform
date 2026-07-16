package com.example.equipment.exception;

public class BlankFieldException extends RuntimeException {
    public BlankFieldException(String fieldName) {
        super(fieldName + " cannot consist of only spaces");
    }
}
