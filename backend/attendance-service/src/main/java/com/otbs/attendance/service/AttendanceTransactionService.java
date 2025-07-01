package com.otbs.attendance.service;

import com.otbs.attendance.dto.AttendanceRecordResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceTransactionService {
    AttendanceRecordResponseDTO getAttendanceRecordByEmployeeIdAndDate(String employeeId, LocalDate date);
    List<AttendanceRecordResponseDTO> getAttendanceRecordsByEmployeeId(String employeeId);
    List<AttendanceRecordResponseDTO> getAttendanceRecordsByDate(String date);
    List<AttendanceRecordResponseDTO> getAllAttendanceRecords();
}
