package com.example.aichatserver.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getField() + ":" + err.getDefaultMessage())
        .findFirst().orElse("bad_request");
    return ResponseEntity.badRequest().body(Map.of(
        "code", "VALIDATION_ERROR",
        "message", msg
    ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception e) {
    return ResponseEntity.status(500).body(Map.of(
        "code", "INTERNAL_ERROR",
        "message", "internal_error"
    ));
  }
}
