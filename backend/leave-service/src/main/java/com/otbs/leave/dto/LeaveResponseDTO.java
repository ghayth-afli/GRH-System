package com.otbs.leave.dto;

import com.otbs.leave.model.ELeaveType;
import com.otbs.leave.model.EStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record LeaveResponseDTO(
        Long id,
        String Name,
        String Department,
        LocalDate startDate,
        LocalDate endDate,
        ELeaveType leaveType,
        EStatus status,
        LocalTime startHOURLY,
        LocalTime endHOURLY
) {
}
