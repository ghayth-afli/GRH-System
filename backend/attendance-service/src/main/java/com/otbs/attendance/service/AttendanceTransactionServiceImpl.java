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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceTransactionServiceImpl implements AttendanceTransactionService {

    // Dependencies
    private final UserClient userClient;
    private final LeaveClient leaveClient;
    private final AttendanceTransactionRepository attendanceTransactionRepository;

    // --- Inlined Attendance Configuration Attributes ---
    private static final LocalTime WORK_START_TIME = LocalTime.parse("08:30");
    private static final LocalTime WORK_END_TIME = LocalTime.parse("17:30");
    private static final LocalTime BREAK_START_TIME = LocalTime.parse("12:30");
    private static final LocalTime BREAK_END_TIME = LocalTime.parse("14:00");
    private static final Duration MINIMUM_REQUIRED_WORK_DURATION = Duration.ofMinutes(450); // 7.5 hours

    // --- NEW: Configuration for Issue Detection ---
    private static final Duration LATE_ARRIVAL_THRESHOLD = Duration.ofMinutes(15);
    private static final Duration EARLY_LEAVE_THRESHOLD = Duration.ofMinutes(30);
    private static final Duration UNUSUAL_BREAK_THRESHOLD = Duration.ofMinutes(120); // 2 hours
    private static final int TARDINESS_LOOKBACK_DAYS = 7;
    private static final int TARDINESS_FREQUENCY_THRESHOLD = 3;

    private final ZoneId systemZoneId = ZoneId.systemDefault();

    @Override
    public List<AttendanceRecordResponseDTO> getAttendanceRecordsByEmployeeId(String employeeId) {
        UserResponse userResponse = getUserByEmployeeId(employeeId);
        if (userResponse == null || userResponse.email() == null) {
            throw new EmployeeException("Cannot find employee for ID: " + employeeId);
        }
        List<AttendanceTransaction> allTransactions = attendanceTransactionRepository.findAllByEmployeeEmail(userResponse.email());

        LocalDate endDate = LocalDate.now(systemZoneId);
        LocalDate startDate = allTransactions.stream()
                .map(t -> t.getPunchTime().atZone(systemZoneId).toLocalDate())
                .min(Comparator.naturalOrder())
                .orElse(endDate);

        Map<LocalDate, List<AttendanceTransaction>> transactionsByDate = allTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getPunchTime().atZone(systemZoneId).toLocalDate()));

        Set<LocalDate> leaveDays = getLeaveDatesForPeriod(userResponse.id(), startDate, endDate);

        List<AttendanceRecordResponseDTO> historicalRecords = new ArrayList<>();

        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            AttendanceRecordResponseDTO dailyRecord;
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                dailyRecord = createStatusResponse(userResponse, date, EStatus.WEEKEND, Set.of());
            } else if (leaveDays.contains(date)) {
                dailyRecord = createStatusResponse(userResponse, date, EStatus.ON_LEAVE, Set.of());
            } else if (transactionsByDate.containsKey(date)) {
                dailyRecord = buildRecordFromTransactions(userResponse, date, transactionsByDate.get(date), historicalRecords);
            } else {
                Set<String> issues = new HashSet<>();
                if(date.isBefore(endDate)){
                    issues.add("No punch recorded");
                }
                EStatus status = (date.equals(endDate) && LocalTime.now(systemZoneId).isBefore(WORK_END_TIME)) ? EStatus.AWAITING : EStatus.ABSENT;
                dailyRecord = createStatusResponse(userResponse, date, status, issues);
            }
            historicalRecords.add(dailyRecord);
        });

        return historicalRecords;
    }

    @Override
    public AttendanceRecordResponseDTO getAttendanceRecordByEmployeeIdAndDate(String employeeId, LocalDate date) {
        UserResponse userResponse = getUserByEmployeeId(employeeId);
        List<AttendanceTransaction> transactions = attendanceTransactionRepository.findByEmployeeEmailAndPunchDate(
                userResponse.email(), date.atStartOfDay(systemZoneId).toInstant(), date.plusDays(1).atStartOfDay(systemZoneId).toInstant());

        if (transactions.isEmpty()) {
            Set<String> issues = new HashSet<>();
            // Only flag "No punch recorded" for past dates on single-day fetches.
            if (date.isBefore(LocalDate.now(systemZoneId))) {
                issues.add("No punch recorded");
            }
            return createStatusResponse(userResponse, date, EStatus.ABSENT, issues);
        }

        // Pass an empty list for historical records as we are only analyzing a single day.
        return buildRecordFromTransactions(userResponse, date, transactions, List.of());
    }

    @Override
    public List<AttendanceRecordResponseDTO> getAttendanceRecordsByDate(String dateString) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new AttendanceException("Invalid date format. Please use YYYY-MM-DD.", e);
        }

        List<UserResponse> allUsers = userClient.getAllUsers();
        if (allUsers == null || allUsers.isEmpty()) {
            return List.of();
        }

        return allUsers.stream()
                .map(user -> getAttendanceRecordByEmployeeIdAndDate(user.id(), date))
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
        allRecords.sort(Comparator.comparing(AttendanceRecordResponseDTO::date));
        return allRecords;
    }

    // Private Helper Methods

    private AttendanceRecordResponseDTO buildRecordFromTransactions(UserResponse user, LocalDate date, List<AttendanceTransaction> transactions, List<AttendanceRecordResponseDTO> historicalRecords) {
        transactions.sort(Comparator.comparing(AttendanceTransaction::getPunchTime));
        List<LocalTime> punchTimes = transactions.stream()
                .map(t -> t.getPunchTime().atZone(systemZoneId).toLocalTime())
                .collect(Collectors.toList());

        LocalTime firstPunch = punchTimes.get(0);
        LocalTime lastPunch = punchTimes.get(punchTimes.size() - 1);
        Duration totalWorkDuration = calculateTotalWorkDuration(punchTimes);
        EStatus status = determineStatus(totalWorkDuration, firstPunch);

        Set<String> issues = detectAttendanceIssues(
                firstPunch, lastPunch, punchTimes, totalWorkDuration, status, historicalRecords
        );

        return new AttendanceRecordResponseDTO(
                user.id(), user.firstName() + " " + user.lastName(), user.department(), date, status,
                firstPunch, lastPunch, formatDuration(totalWorkDuration), punchTimes, punchTimes.size(), issues
        );
    }

    private Set<String> detectAttendanceIssues(LocalTime firstPunch, LocalTime lastPunch, List<LocalTime> punchTimes, Duration totalWork, EStatus status, List<AttendanceRecordResponseDTO> historicalRecords) {
        Set<String> issues = new HashSet<>();

        if (firstPunch.isAfter(WORK_START_TIME.plus(LATE_ARRIVAL_THRESHOLD))) {
            issues.add("Arrived very late");
        }

        if (punchTimes.size() > 1 && lastPunch.isBefore(WORK_END_TIME.minus(EARLY_LEAVE_THRESHOLD))) {
            issues.add("Left very early");
        }

        if (punchTimes.size() % 2 != 0) {
            issues.add("Missed last punch");
        }

        if (status != EStatus.HALF_DAY && totalWork.compareTo(MINIMUM_REQUIRED_WORK_DURATION) < 0) {
            issues.add("Worked fewer hours than required");
        }

        Duration actualBreak = calculateActualBreakDuration(punchTimes);
        if (actualBreak.compareTo(UNUSUAL_BREAK_THRESHOLD) > 0) {
            issues.add("Unusually long break");
        }

        if (status == EStatus.LATE || issues.contains("Arrived very late")) {
            long recentLatenessCount = historicalRecords.stream()
                    .filter(r -> r.date().isAfter(LocalDate.now(systemZoneId).minusDays(TARDINESS_LOOKBACK_DAYS)))
                    .filter(r -> r.status() == EStatus.LATE || r.issues().contains("Arrived very late"))
                    .count();

            if (recentLatenessCount + 1 >= TARDINESS_FREQUENCY_THRESHOLD) {
                issues.add("Frequent tardiness");
            }
        }

        return issues;
    }

    private EStatus determineStatus(Duration totalWorkDuration, LocalTime firstPunch) {
        if (totalWorkDuration.toHours() < 4 || firstPunch.isAfter(BREAK_START_TIME)) {
            return EStatus.HALF_DAY;
        } else if (firstPunch.isAfter(WORK_START_TIME)) {
            return EStatus.LATE;
        } else {
            return EStatus.PRESENT;
        }
    }

    private Duration calculateActualBreakDuration(List<LocalTime> punchTimes) {
        if (punchTimes.size() <= 2) return Duration.ZERO;

        Duration totalBreak = Duration.ZERO;
        for (int i = 1; i < punchTimes.size() - 1; i += 2) {
            LocalTime breakStart = punchTimes.get(i);
            LocalTime breakEnd = punchTimes.get(i + 1);
            totalBreak = totalBreak.plus(Duration.between(breakStart, breakEnd));
        }
        return totalBreak;
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

    private Set<LocalDate> getLeaveDatesForPeriod(String userId, LocalDate start, LocalDate end) {
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

    private AttendanceRecordResponseDTO createStatusResponse(UserResponse user, LocalDate date, EStatus status, Set<String> issues) {
        return new AttendanceRecordResponseDTO(
                user.id(), user.firstName() + " " + user.lastName(), user.department(), date, status,
                null, null, "0:00", List.of(), 0, issues
        );
    }
}