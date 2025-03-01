package com.otbs.employee.exception;

import com.otbs.employee.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({EmployeeNotFoundException.class})
    public ResponseEntity<?> handleEmployeeNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Employee not found"));
    }
}
