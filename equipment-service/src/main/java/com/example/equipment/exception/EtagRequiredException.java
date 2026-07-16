package com.example.equipment.exception;

public class EtagRequiredException extends RuntimeException {
    public EtagRequiredException() {
        super("If-Match header is required");
    }
}
