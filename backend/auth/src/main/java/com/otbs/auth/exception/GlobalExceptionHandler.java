package com.otbs.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({InvalidTokenException.class})
    public ResponseEntity<?> handleInvalidToken() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token");
    }

    @ExceptionHandler({TokenExpiredException.class})
    public ResponseEntity<?> handleExpiredToken() {
        return ResponseEntity.status(HttpStatus.GONE).body("Token expired");
    }
}
