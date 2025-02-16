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

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<?> handleUserNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @ExceptionHandler({MailFailedException.class})
    public ResponseEntity<?> handleMailFailed() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email");
    }
}
