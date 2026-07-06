package com.backend.smarthire.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(
                Map.of(
                        "success", false,
                        "error", e.getClass().getSimpleName(),
                        "message", e.getMessage() != null ? e.getMessage() : "No message provided"
                )
        );
    }
}
