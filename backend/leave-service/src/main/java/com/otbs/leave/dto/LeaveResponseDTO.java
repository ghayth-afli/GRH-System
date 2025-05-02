package com.otbs.leave.dto;

import com.otbs.leave.model.ELeaveType;
import com.otbs.leave.model.EStatus;

import java.time.LocalDate;

public record LeaveResponseDTO(
        Long id,
        String Name,
        String Department,
        LocalDate startDate,
        LocalDate endDate,
        ELeaveType leaveType,
        EStatus status
) {
}
