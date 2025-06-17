package com.otbs.leave.dto;

import com.otbs.leave.exception.*;
import com.otbs.leave.model.ELeaveType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(
        description = "DTO for submitting a leave request",
        requiredProperties = {"leaveType", "startDate", "endDate"}
)
public record LeaveRequestDTO(

        @NotBlank(message = "Leave type is required")
        @Schema(description = "Type of leave (e.g., ANNUAL, SICK, MATERNITY)", example = "ANNUAL")
        ELeaveType leaveType,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date must be today or in the future")
        @Schema(description = "Start date of the leave", example = "2025-06-01")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @FutureOrPresent(message = "End date must be today or in the future")
        @Schema(description = "End date of the leave", example = "2025-06-05")
        LocalDate endDate,

        @Schema(description = "Start time for hourly leave (required for AUTHORIZATION leave)", example = "09:00:00")
        LocalTime startHOURLY,

        @Schema(description = "End time for hourly leave (required for AUTHORIZATION leave)", example = "17:00:00")
        LocalTime endHOURLY
) {
    public LeaveRequestDTO {
        validateDates(startDate, endDate);
        validateTimes(startHOURLY, endHOURLY);
        // CHANGED: Updated validation for authorization leave
        validateAuthorizationLeave(leaveType, startDate, endDate, startHOURLY, endHOURLY);
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new DateRangeException("Start date must be before or equal to end date");
        }
    }

    private boolean isSpecialLeaveType(ELeaveType leaveType) {
        return leaveType == ELeaveType.DÉCÈS || leaveType == ELeaveType.MATERNITÉ ||
                leaveType == ELeaveType.PATERNITÉ || leaveType == ELeaveType.MALADIE;
    }

    private void validateTimes(LocalTime startHOURLY, LocalTime endHOURLY) {
        if (startHOURLY != null && endHOURLY != null && startHOURLY.isAfter(endHOURLY)) {
            throw new TimeRangeException("Start time must be before or equal to end time");
        }
    }

    // CHANGED: Added startDate and endDate to the validation method
    private void validateAuthorizationLeave(ELeaveType leaveType, LocalDate startDate, LocalDate endDate, LocalTime startHOURLY, LocalTime endHOURLY) {
        if (leaveType == ELeaveType.AUTORISATION) {
            if (startHOURLY == null || endHOURLY == null) {
                throw new MissingTimeForAuthorizationException("Start and end time are required for authorization leave type");
            }
            // NEW: Enforce that for authorization leave, start and end dates must be the same
            if (!startDate.equals(endDate)) {
                throw new DateRangeException("For AUTORISATION leave, start date and end date must be the same day.");
            }
        }
    }
}