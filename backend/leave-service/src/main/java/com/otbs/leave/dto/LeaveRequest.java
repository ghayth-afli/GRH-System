package com.otbs.leave.dto;

import com.otbs.leave.exception.EmptyAttachmentException;
import com.otbs.leave.exception.InvalidDateRangeException;
import com.otbs.leave.exception.InvalidTimeRangeException;
import com.otbs.leave.exception.MissingTimeForAuthorizationException;
import com.otbs.leave.model.ELeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;
import java.time.LocalTime;

public record LeaveRequest(
        @NotBlank(message = "Leave type is required") ELeaveType leaveType,



        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future") LocalDate startDate,

        @NotNull(message = "End date is required")
        @FutureOrPresent(message = "End date must be today or in the future") LocalDate endDate,

        //byte[] attachment,

        LocalTime startHOURLY,

        LocalTime endHOURLY
) {
    public LeaveRequest {
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException("Start date must be before or equal to end date");
        }
        /*if (attachment != null && attachment.length == 0) {
            throw new EmptyAttachmentException("Attachment cannot be empty");
        }*/
        if (startHOURLY != null && endHOURLY != null && startHOURLY.isAfter(endHOURLY)) {
            throw new InvalidTimeRangeException("Start time must be before or equal to end time");
        }
        if (leaveType == ELeaveType.AUTORISATION && (startHOURLY == null || endHOURLY == null)) {
            throw new MissingTimeForAuthorizationException("Start and end time are required for authorization leave type");
        }
    }
}


