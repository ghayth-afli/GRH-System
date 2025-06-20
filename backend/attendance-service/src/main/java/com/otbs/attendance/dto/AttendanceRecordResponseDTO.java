package com.otbs.attendance.dto;

import com.otbs.attendance.model.EStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record AttendanceRecordResponseDTO(
        String employeeId,
        String employeeName,
        String department,
        LocalDate date,
        EStatus status,
        LocalTime firstPunch,
        LocalTime lastPunch,
        String totalHours,
        List<LocalTime> allPunches,
        int punchCount,
        Set<String> issues
) {}