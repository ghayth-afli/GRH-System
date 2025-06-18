package com.otbs.attendance.controller;

import com.otbs.attendance.dto.AttendanceRecordResponseDTO;
import com.otbs.attendance.service.AttendanceTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "APIs for fetching employee attendance records")
public class AttendanceRecordController {

    private final AttendanceTransactionService attendanceService;

    @Operation(summary = "Get all attendance records, optionally filtered by date",
            description = "Retrieves a list of all attendance records. If a 'date' parameter is provided, it returns all records for that specific date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved records"),
                    @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content)
            })
    @GetMapping("/attendance-records")
    public ResponseEntity<List<AttendanceRecordResponseDTO>> getAllAttendanceRecords(
            @Parameter(description = "Optional date to filter records (format: yyyy-MM-dd)", example = "2024-10-28")
            @RequestParam(name = "date", required = false) String date) {

        if (date != null && !date.isBlank()) {
            return ResponseEntity.ok(attendanceService.getAttendanceRecordsByDate(date));
        } else {
            return ResponseEntity.ok(attendanceService.getAllAttendanceRecords());
        }
    }

    @Operation(summary = "Get all attendance records for a specific employee",
            description = "Retrieves the complete attendance history for a single employee, identified by their ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved records"),
                    @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
            })
    @GetMapping("/employees/{employeeId}/attendance-records")
    public ResponseEntity<List<AttendanceRecordResponseDTO>> getAttendanceRecordsForEmployee(
            @Parameter(description = "The unique identifier of the employee", required = true, example = "cn=johndoe,ou=users,dc=otbs,dc=com")
            @PathVariable String employeeId) {

        List<AttendanceRecordResponseDTO> records = attendanceService.getAttendanceRecordsByEmployeeId(employeeId);
        return ResponseEntity.ok(records);
    }

    @Operation(summary = "Get a single day's attendance record for an employee",
            description = "Retrieves the detailed attendance record for a specific employee on a single given date.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the record"),
                    @ApiResponse(responseCode = "400", description = "Invalid date format", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Employee not found", content = @Content)
            })
    @GetMapping("/employees/{employeeId}/attendance-records/{date}")
    public ResponseEntity<AttendanceRecordResponseDTO> getAttendanceRecordForEmployeeByDate(
            @Parameter(description = "The unique identifier of the employee", required = true, example = "cn=janjansen,ou=users,dc=otbs,dc=com")
            @PathVariable String employeeId,

            @Parameter(description = "The specific date for the record (format: yyyy-MM-dd)", required = true, example = "2024-10-28")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AttendanceRecordResponseDTO record = attendanceService.getAttendanceRecordByEmployeeIdAndDate(employeeId, date);
        return ResponseEntity.ok(record);
    }
}