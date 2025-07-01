package com.otbs.attendance.exception;

public class AttendanceException extends RuntimeException {

    public AttendanceException(String message) {
        super(message);
    }

    public AttendanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttendanceException(Throwable cause) {
        super(cause);
    }
}
