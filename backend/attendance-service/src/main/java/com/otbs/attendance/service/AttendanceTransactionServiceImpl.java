package com.otbs.attendance.service;

import com.otbs.attendance.dto.AttendanceRecordResponseDTO;
import com.otbs.attendance.exception.AttendanceException;
import com.otbs.attendance.exception.EmployeeException;
import com.otbs.attendance.model.AttendanceTransaction;
import com.otbs.attendance.model.EStatus;
import com.otbs.attendance.repository.AttendanceTransactionRepository;
import com.otbs.feign.client.leave.LeaveClient;
import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceTransactionServiceImpl implements AttendanceTransactionService {

    // Feign Clients and Repositories
    private final UserClient userClient;
    private final LeaveClient leaveClient;
    private final AttendanceTransactionRepository attendanceTransactionRepository;

    // --- Inlined Attendance Configuration Attributes ---
    private static final LocalTime WORK_START_TIME = LocalTime.parse("08:30");
    private static final LocalTime WORK_END_TIME = LocalTime.parse("17:30");
    private static final LocalTime BREAK_START_TIME = LocalTime.parse("12:30");
    private static final LocalTime BREAK_END_TIME = LocalTime.parse("14:00");
    private static final int HALF_DAY_THRESHOLD_HOURS = 4;

    // System's default ZoneId, defined once for consistency
    private final ZoneId systemZoneId = ZoneId.systemDefault();

    @Override
    public AttendanceRecordResponseDTO getAttendanceRecordByEmployeeIdAndDate(String employeeId, LocalDate date) {
        UserResponse userResponse = getUserByEmployeeId(employeeId);
        // This method is now primarily for fetching single, specific day records.
        // The logic for gap-filling is handled in getAttendanceRecordsByEmployeeId.
        return buildRecordForSingleDay(userResponse, date);
    }

    /**
     * Retrieves a complete, gap-less list of attendance records for an employee,
     * starting from their earliest punch date.
     */
    @Override
    public List<AttendanceRecordResponseDTO> getAttendanceRecordsByEmployeeId(String employeeId) {
        // Step 1: Fetch user and all their transactions in one go.
        UserResponse userResponse = getUserByEmployeeId(employeeId);
        if (userResponse == null || userResponse.email() == null) {
            throw new EmployeeException("Cannot find employee or employee email for ID: " + employeeId);
        }
        List<AttendanceTransaction> allTransactions = attendanceTransactionRepository.findAllByEmployeeEmail(userResponse.email());

        LocalDate endDate = LocalDate.now(systemZoneId);

        // Step 2: Determine the start date from the earliest punch record.
        LocalDate startDate = allTransactions.stream()
                .map(t -> t.getPunchTime().atZone(systemZoneId).toLocalDate())
                .min(LocalDate::compareTo)
                .orElse(endDate); // If no punches ever, just show today's record.

        // Step 3: Group existing transactions by date for quick lookup.
        Map<LocalDate, List<AttendanceTransaction>> transactionsByDate = allTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getPunchTime().atZone(systemZoneId).toLocalDate()));

        // PERFORMANCE OPTIMIZATION: Fetch all leave days in the date range at once.
        Set<LocalDate> leaveDays = getLeaveDatesForPeriod(userResponse.id(), startDate, endDate);

        // Step 4: Generate the complete, gap-less record stream from start date to today.
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    DayOfWeek dayOfWeek = date.getDayOfWeek();
                    // Priority 1: Weekends
                    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                        return createStatusResponse(userResponse, date, EStatus.WEEKEND);
                    }
                    // Priority 2: Approved Leave
                    if (leaveDays.contains(date)) {
                        return createStatusResponse(userResponse, date, EStatus.ON_LEAVE);
                    }
                    // Priority 3: Days with actual punches
                    if (transactionsByDate.containsKey(date)) {
                        return buildRecordFromTransactions(userResponse, date, transactionsByDate.get(date));
                    }
                    // Priority 4: Today's real-time status (if no punches yet)
                    if (date.equals(endDate)) {
                        return createStatusResponse(userResponse, date,
                                LocalTime.now(systemZoneId).isBefore(WORK_END_TIME) ? EStatus.AWAITING : EStatus.ABSENT);
                    }
                    // Default: Any other past day with no activity is ABSENT
                    return createStatusResponse(userResponse, date, EStatus.ABSENT);
                })
                .sorted(Comparator.comparing(AttendanceRecordResponseDTO::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordResponseDTO> getAttendanceRecordsByDate(String dateString) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new AttendanceException("Invalid date format. Please use yyyy-MM-dd.", e);
        }

        List<UserResponse> allUsers = userClient.getAllUsers();
        if (allUsers == null || allUsers.isEmpty()) {
            return List.of();
        }

        return allUsers.stream()
                .map(user -> buildRecordForSingleDay(user, date))
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceRecordResponseDTO> getAllAttendanceRecords() {
        List<UserResponse> allUsers = userClient.getAllUsers();
        if (allUsers == null || allUsers.isEmpty()) {
            return List.of();
        }
        List<AttendanceRecordResponseDTO> allRecords = new ArrayList<>();
        for (UserResponse user : allUsers) {
            try {
                allRecords.addAll(getAttendanceRecordsByEmployeeId(user.id()));
            } catch (EmployeeException e) {
                System.err.println("Could not process records for employee ID " + user.id() + ": " + e.getMessage());
            }
        }
        allRecords.sort(Comparator.comparing(AttendanceRecordResponseDTO::getDate));
        return allRecords;
    }

    // ===================================================================================
    // HELPER METHODS
    // ===================================================================================

    /**
     * Builds a record for a single day, fetching its own data.
     * Used by getAttendanceRecordByEmployeeIdAndDate.
     */
    private AttendanceRecordResponseDTO buildRecordForSingleDay(UserResponse userResponse, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return createStatusResponse(userResponse, date, EStatus.WEEKEND);
        }
        if (isLeaveOnDate(userResponse.id(), date)) {
            return createStatusResponse(userResponse, date, EStatus.ON_LEAVE);
        }
        Instant startOfDay = date.atStartOfDay(systemZoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(systemZoneId).toInstant();
        List<AttendanceTransaction> transactions = attendanceTransactionRepository.findByEmployeeEmailAndPunchDate(
                userResponse.email(), startOfDay, endOfDay);

        if(transactions.isEmpty()) {
            if (date.equals(LocalDate.now(systemZoneId))) {
                return createStatusResponse(userResponse, date,
                        LocalTime.now(systemZoneId).isBefore(WORK_END_TIME) ? EStatus.AWAITING : EStatus.ABSENT);
            }
            return createStatusResponse(userResponse, date, EStatus.ABSENT);
        }
        return buildRecordFromTransactions(userResponse, date, transactions);
    }

    /**
     * Private helper to build a DTO from pre-fetched transactions.
     * This avoids redundant DB calls and is used by the main gap-filling logic.
     */
    private AttendanceRecordResponseDTO buildRecordFromTransactions(UserResponse user, LocalDate date, List<AttendanceTransaction> transactions) {
        transactions.sort(Comparator.comparing(AttendanceTransaction::getPunchTime));

        List<LocalTime> punchTimes = transactions.stream()
                .map(transaction -> transaction.getPunchTime().atZone(systemZoneId).toLocalTime())
                .collect(Collectors.toList());

        LocalTime firstPunch = punchTimes.getFirst();
        LocalTime lastPunch = punchTimes.getLast();
        Duration totalWorkDuration = calculateTotalWorkDuration(punchTimes);
        String totalHoursFormatted = formatDuration(totalWorkDuration);

        EStatus status;
        if (totalWorkDuration.toHours() < HALF_DAY_THRESHOLD_HOURS || firstPunch.isAfter(BREAK_START_TIME)) {
            status = EStatus.HALF_DAY;
        } else if (firstPunch.isAfter(WORK_START_TIME)) {
            status = EStatus.LATE;
        } else {
            status = EStatus.PRESENT;
        }

        return new AttendanceRecordResponseDTO(user.id(), user.firstName() + " " + user.lastName(),
                user.department(), date, status, firstPunch, lastPunch, totalHoursFormatted, punchTimes, transactions.size());
    }

    private Duration calculateTotalWorkDuration(List<LocalTime> punchTimes) {
        if (punchTimes.size() < 2) return Duration.ZERO;
        Duration totalDuration = Duration.ZERO;
        for (int i = 0; i < punchTimes.size(); i += 2) {
            if (i + 1 >= punchTimes.size()) break;
            LocalTime punchIn = punchTimes.get(i);
            LocalTime punchOut = punchTimes.get(i + 1);
            LocalTime effectiveBreakStart = punchIn.isAfter(BREAK_START_TIME) ? punchIn : BREAK_START_TIME;
            LocalTime effectiveBreakEnd = punchOut.isBefore(BREAK_END_TIME) ? punchOut : BREAK_END_TIME;
            Duration segmentDuration = Duration.between(punchIn, punchOut);
            Duration breakOverlap = Duration.ZERO;
            if (effectiveBreakEnd.isAfter(effectiveBreakStart)) {
                breakOverlap = Duration.between(effectiveBreakStart, effectiveBreakEnd);
            }
            totalDuration = totalDuration.plus(segmentDuration.minus(breakOverlap));
        }
        return totalDuration;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%d:%02d", hours, minutes);
    }

    /**
     * Placeholder for an efficient bulk leave-checking operation.
     * Ideally, your LeaveClient should have a method that takes a date range.
     */
    private Set<LocalDate> getLeaveDatesForPeriod(String userId, LocalDate start, LocalDate end) {
        // This is where you would call your leave microservice.
        // For example: return leaveClient.getLeavesForEmployeeBetween(userId, start, end).getBody();
        // Simulating by checking each day individually (the less performant way).
        return start.datesUntil(end.plusDays(1))
                .filter(date -> isLeaveOnDate(userId, date))
                .collect(Collectors.toSet());
    }

    private boolean isLeaveOnDate(String userDn, LocalDate date) {
        var response = leaveClient.leaveExists(userDn, date);
        return response.getBody() != null && Boolean.TRUE.equals(response.getBody());
    }

    private UserResponse getUserByEmployeeId(String employeeId) {
        return userClient.getUserByDn(employeeId);
    }

    private AttendanceRecordResponseDTO createStatusResponse(UserResponse user, LocalDate date, EStatus status) {
        return new AttendanceRecordResponseDTO(user.id(), user.firstName() + " " + user.lastName(),
                user.department(), date, status, null, null, "0:00", List.of(), 0);
    }
}