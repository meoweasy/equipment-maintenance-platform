package com.example.equipment.exception;

public class EtagMismatchException extends RuntimeException {
    public EtagMismatchException() {
        super("ETag does not match the current resource version");
    }
}
