package com.otbs.attendance.service;

import com.otbs.attendance.dto.AttendanceRecordResponseDTO;
import com.otbs.attendance.exception.AttendanceException;
import com.otbs.attendance.exception.EmployeeException;
import com.otbs.attendance.model.AttendanceTransaction;
import com.otbs.attendance.model.EStatus;
import com.otbs.attendance.model.Employee;
import com.otbs.attendance.repository.AttendanceTransactionRepository;
import com.otbs.attendance.repository.EmployeeRepository;
import com.otbs.feign.client.leave.LeaveClient;
import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceTransactionServiceImpl implements AttendanceTransactionService {

    private final UserClient userClient;
    private final LeaveClient leaveClient;
    private final AttendanceTransactionRepository attendanceTransactionRepository;
    private final EmployeeRepository employeeRepository;
    private static final String WORK_HOURS_START = "08:30";
    private static final String WORK_HOURS_END = "17:30";

    @Override
    public AttendanceRecordResponseDTO getAttendanceRecordByEmployeeIdAndDate(String employeeId, LocalDate date) {
        UserResponse userResponse = getUserByEmployeeId(employeeId);

        if (userResponse == null) {
            throw new EmployeeException("Employee not found with ID: " + employeeId);
        }

        if (userResponse.email() == null || userResponse.email().isEmpty()) {
            throw new EmployeeException("Employee email not found for ID: " + employeeId);
        }


        if (isLeaveOnDate(userResponse.id(), date)) {
            return new AttendanceRecordResponseDTO(
                    //employee,
                    userResponse.id(),
                    userResponse.firstName() + " " + userResponse.lastName(),
                    date,
                    EStatus.ON_LEAVE,
                    null,
                    null,
                    null,
                    null,
                    0
            );
        }

        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);
        List<AttendanceTransaction> transactions = attendanceTransactionRepository.findByEmployeeEmailAndPunchDate(
                userResponse.email(),
                startOfDay,
                endOfDay
        );

        // Order transactions by punch time (already handled by repository query, but good practice to ensure)
        transactions.sort((t1, t2) -> t1.getPunchTime().compareTo(t2.getPunchTime()));

        if (transactions.isEmpty()) {
            return new AttendanceRecordResponseDTO(
                    //employee,
                    userResponse.id(),
                    userResponse.firstName() + " " + userResponse.lastName(),
                    date,
                    EStatus.ABSENT,
                    null,
                    null,
                    null,
                    null,
                    0
            );
        }

        List<LocalTime> punchTimes = transactions.stream()
                .map(transaction -> transaction.getPunchTime()
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime())
                .toList();

        LocalTime firstPunch = punchTimes.get(0);
        LocalTime lastPunch = punchTimes.get(punchTimes.size() - 1);

        String totalHours = calculateTotalHours(punchTimes);

        LocalTime workStartTime = LocalTime.parse(WORK_HOURS_START);
        EStatus status = firstPunch.isBefore(workStartTime) || firstPunch.equals(workStartTime)
                ? EStatus.PRESENT
                : EStatus.LATE;

        return new AttendanceRecordResponseDTO(
                //employee,
                userResponse.id(),
                userResponse.firstName() + " " + userResponse.lastName(),
                date,
                status,
                firstPunch,
                lastPunch,
                totalHours,
                punchTimes,
                transactions.size()
        );
    }

    /**
     * Retrieves all attendance records for a specific employee across all dates.
     * It first finds all unique dates the employee has punch records for,
     * then calculates the attendance for each of those dates.
     *
     * @param employeeId The unique identifier for the employee.
     * @return A list of daily attendance records.
     */
    @Override
    public List<AttendanceRecordResponseDTO> getAttendanceRecordsByEmployeeId(String employeeId) {
        UserResponse userResponse = getUserByEmployeeId(employeeId);
        if (userResponse == null || userResponse.email() == null) {
            throw new EmployeeException("Cannot find employee or employee email for ID: " + employeeId);
        }

        // Find all transactions for the employee to determine the active dates.
        List<AttendanceTransaction> allTransactions = attendanceTransactionRepository.findAllByEmployeeEmail(userResponse.email());

        // Extract the unique dates from the transactions.
        Set<LocalDate> uniqueDates = allTransactions.stream()
                .map(transaction -> transaction.getPunchTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());

        // For each unique date, call the detailed method to get the attendance record.
        return uniqueDates.stream()
                .sorted() // Sort dates chronologically
                .map(date -> getAttendanceRecordByEmployeeIdAndDate(employeeId, date))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the attendance records for all employees on a specific date.
     *
     * @param dateString The date in 'YYYY-MM-DD' format.
     * @return A list of attendance records for the given date.
     */
    @Override
    public List<AttendanceRecordResponseDTO> getAttendanceRecordsByDate(String dateString) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new AttendanceException("Invalid date format. Please use YYYY-MM-DD.", e);
        }

        // We need a list of all employees to check their attendance for the given date.
        // This assumes your userClient can provide all users.
        List<UserResponse> allUsers = userClient.getAllUsers();
        if (allUsers == null || allUsers.isEmpty()) {
            return List.of(); // Return empty list if no users are found
        }

        final LocalDate finalDate = date; // Variable used in lambda must be final or effectively final

        // For each user, get their attendance record for that specific date.
        return allUsers.stream()
                .map(user -> getAttendanceRecordByEmployeeIdAndDate(user.id(), finalDate))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all attendance records for all employees across all their active dates.
     * Note: This can be a very resource-intensive operation and may be slow
     * if there is a large amount of data.
     *
     * @return A comprehensive list of all attendance records.
     */
    @Override
    public List<AttendanceRecordResponseDTO> getAllAttendanceRecords() {
        List<UserResponse> allUsers = userClient.getAllUsers();
        if (allUsers == null || allUsers.isEmpty()) {
            return List.of();
        }

        List<AttendanceRecordResponseDTO> allRecords = new ArrayList<>();
        // Iterate through each user and get all their historical attendance records.
        for (UserResponse user : allUsers) {
            try {
                // Reuse the logic from getAttendanceRecordsByEmployeeId
                List<AttendanceRecordResponseDTO> employeeRecords = getAttendanceRecordsByEmployeeId(user.id());
                allRecords.addAll(employeeRecords);
            } catch (EmployeeException e) {
                // Log the exception or handle it as needed.
                // For example, you might want to continue processing other employees.
                System.err.println("Could not process records for employee ID " + user.id() + ": " + e.getMessage());
            }
        }
        return allRecords;
    }

    private String calculateTotalHours(List<LocalTime> punchTimes) {
        if (punchTimes.size() < 2) {
            return "0:00";
        }

        long totalMinutes = 0;
        LocalTime workEndTime = LocalTime.parse(WORK_HOURS_END);

        for (int i = 0; i < punchTimes.size(); i += 2) {
            LocalTime punchIn = punchTimes.get(i);
            // If there's an odd number of punches, the last punch-in is paired with the standard work end time.
            LocalTime punchOut = (i + 1 < punchTimes.size()) ? punchTimes.get(i + 1) : workEndTime;

            long minutes = java.time.Duration.between(punchIn, punchOut).toMinutes();
            if (minutes > 0) {
                totalMinutes += minutes;
            }
        }

        long hours = totalMinutes / 60;
        long remainingMinutes = totalMinutes % 60;

        return String.format("%d:%02d", hours, remainingMinutes);
    }

    private UserResponse getUserByEmployeeId(String employeeId) {
        // Assuming userClient handles potential Feign exceptions (e.g., 404 Not Found)
        return userClient.getUserByDn(employeeId);
    }

    private boolean isLeaveOnDate(String userDn, LocalDate date) {
        // The double call can be optimized by storing the response.
        var response = leaveClient.leaveExists(userDn, date);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody());
    }
}