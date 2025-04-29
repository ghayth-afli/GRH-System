package com.otbs.leave.exception;

import com.otbs.leave.dto.MessageResponseDTO;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({DateRangeException.class})
    public ResponseEntity<?> handleInvalidDateRange(DateRangeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({TimeRangeException.class})
    public ResponseEntity<?> handleInvalidTimeRange(TimeRangeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({MissingTimeForAuthorizationException.class})
    public ResponseEntity<?> handleMissingTimeForAuthorization(MissingTimeForAuthorizationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({LeaveBalanceException.class})
    public ResponseEntity<?> handleInsufficientLeaveBalance(LeaveBalanceException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({LeaveException.class})
    public ResponseEntity<?> handleLeaveNotFound(LeaveException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({UserException.class})
    public ResponseEntity<?> handleUserNotFound(UserException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({FileUploadException.class})
    public ResponseEntity<?> handleFileUploadException(FileUploadException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler({AttachmentException.class})
    public ResponseEntity<?> handleAttachmentNotFound(AttachmentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<?> handleFeignException(FeignException e) {
        return ResponseEntity.status(e.status()).body(new MessageResponseDTO(e.getMessage()));
    }
}
