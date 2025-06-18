package com.otbs.attendance.dto;

import com.otbs.attendance.model.EStatus;
import com.otbs.attendance.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class AttendanceRecordResponseDTO {
    private String employeeId;
    private String employeeName;
    private LocalDate date;
    private EStatus status;
    private LocalTime firstPunch;
    private LocalTime lastPunch;
    private String totalHours;
    private List<LocalTime> punchTimes;
    private Integer punches;
}
