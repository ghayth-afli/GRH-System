package com.otbs.leave.exception;

import com.otbs.leave.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({InvalidDateRangeException.class})
    public ResponseEntity<?> handleInvalidDateRange() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Invalid date range"));
    }

    @ExceptionHandler({InvalidTimeRangeException.class})
    public ResponseEntity<?> handleInvalidTimeRange() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Invalid time range"));
    }

    @ExceptionHandler({MissingTimeForAuthorizationException.class})
    public ResponseEntity<?> handleMissingTimeForAuthorization() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Missing time for authorization"));
    }

    @ExceptionHandler({EmptyAttachmentException.class})
    public ResponseEntity<?> handleEmptyAttachment() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Empty attachment"));
    }
}
