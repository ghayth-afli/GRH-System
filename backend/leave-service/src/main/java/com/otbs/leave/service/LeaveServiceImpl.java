package com.otbs.leave.service;

import com.otbs.feign.client.EmployeeClient;
import com.otbs.feign.dto.EmployeeResponse;
import com.otbs.leave.dto.LeaveRequestDTO;
import com.otbs.leave.dto.LeaveResponseDTO;
import com.otbs.leave.exception.*;
import com.otbs.leave.mapper.LeaveAttributesMapper;
import com.otbs.leave.model.*;
import com.otbs.leave.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRepository leaveRepository;
    private final LeaveNotificationService leaveNotificationService;
    private final LeaveAttributesMapper leaveAttributesMapper;
    private final EmployeeClient employeeClient;

    @Override
    public void applyLeave(LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        validateLeaveRequest(leaveRequestDTO);

        String userDn = getCurrentUserDn();
        EmployeeResponse user = fetchEmployee(userDn);

        Leave leave = leaveAttributesMapper.toEntity(leaveRequestDTO);
        leave.setUserDn(user.id());
        processAttachment(leave, attachment);
        validateLeaveDateRange(leave);

        leaveRepository.save(leave);

        sendAsyncNotifications(user, leave);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncNotifications(EmployeeResponse user, Leave leave) {
        EmployeeResponse manager = fetchManager(user.department());

        CompletableFuture<Void> userNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendMailNotification(
                        user.email(),
                        "Leave Application",
                        "Your leave application has been submitted successfully"
                ));

        CompletableFuture<Void> managerNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendMailNotification(
                        manager.email(),
                        "Leave Application",
                        String.format("A new leave request has been submitted by %s %s",
                                user.firstName(), user.lastName())
                ));

        CompletableFuture<Void> leaveNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendLeaveNotification(
                        manager.id(),
                        "Leave Application",
                        String.format("A new leave request has been submitted by %s %s",
                                user.firstName(), user.lastName()),
                        leave.getId()
                ));

        CompletableFuture.allOf(userNotification, managerNotification, leaveNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send notifications for leave ID {}: {}",
                                leave.getId(), throwable.getMessage());
                    } else {
                        log.debug("Notifications sent successfully for leave ID {}", leave.getId());
                    }
                });
    }

    @Override
    public void cancelLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new LeaveException("Leave not found"));

        leaveRepository.delete(leave);
        EmployeeResponse emp = fetchEmployee(leave.getUserDn());
        leaveNotificationService.sendMailNotification(
                emp.email(),
                "Leave Application Cancelled",
                "Your leave application has been successfully cancelled"
        );
    }

    @Override
    public void approveLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new LeaveException("Leave not found"));

        leave.setStatus(EStatus.APPROUVÉE);
        leaveRepository.save(leave);
        updateLeaveBalance(leave);
        EmployeeResponse emp = fetchEmployee(leave.getUserDn());
        sendAsyncApprovalNotifications(emp,leave);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncApprovalNotifications(EmployeeResponse user,Leave leave) {
        CompletableFuture<Void> mailNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendMailNotification(
                        user.email(),
                        "Leave Application Approved",
                        "Your leave application has been approved"
                ));

        CompletableFuture<Void> leaveNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendLeaveNotification(
                        leave.getUserDn(),
                        "Leave Application Approved",
                        String.format("Your leave application for %s has been approved", leave.getStartDate()),
                        leave.getId()
                ));

        CompletableFuture.allOf(mailNotification, leaveNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send approval notifications for leave ID {}: {}",
                                leave.getId(), throwable.getMessage());
                    } else {
                        log.debug("Approval notifications sent successfully for leave ID {}", leave.getId());
                    }
                });
    }

    @Override
    public void rejectLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new LeaveException("Leave not found"));

        leave.setStatus(EStatus.REFUSÉE);
        leaveRepository.save(leave);
        EmployeeResponse emp = fetchEmployee(leave.getUserDn());
        sendAsyncRejectionNotifications(emp,leave);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncRejectionNotifications(EmployeeResponse user,Leave leave) {
        CompletableFuture<Void> mailNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendMailNotification(
                        user.email(),
                        "Leave Application Rejected",
                        "Your leave application has been rejected"
                ));

        CompletableFuture<Void> leaveNotification = CompletableFuture.runAsync(() ->
                leaveNotificationService.sendLeaveNotification(
                        leave.getUserDn(),
                        "Leave Application Rejected",
                        String.format("Your leave application for %s has been rejected", leave.getStartDate()),
                        leave.getId()
                ));

        CompletableFuture.allOf(mailNotification, leaveNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send rejection notifications for leave ID {}: {}",
                                leave.getId(), throwable.getMessage());
                    } else {
                        log.debug("Rejection notifications sent successfully for leave ID {}", leave.getId());
                    }
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Leave> getLeaveHistory(String userDn) {
        return leaveRepository.findAllByUserDn(userDn);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalance getLeaveBalance(String userDn) {
        return leaveBalanceRepository.findByUserDn(userDn)
                .orElseThrow(() -> new LeaveBalanceException("Leave balance not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getAllLeaves() {
        String userDn = getCurrentUserDn();
        EmployeeResponse user = fetchEmployee(userDn);

        return leaveRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(leave -> !isManager(user) || isSameDepartment(user, leave.getUserDn()))
                .map(leaveAttributesMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Leave> getAllLeavesForManager(Pageable pageable) {
        String userDn = getCurrentUserDn();
        EmployeeResponse user = fetchEmployee(userDn);

        List<Leave> filteredLeaves = leaveRepository.findAll(pageable).stream()
                .filter(leave -> isSameDepartment(user, leave.getUserDn()))
                .collect(Collectors.toList());

        return new PageImpl<>(filteredLeaves, pageable, filteredLeaves.size());
    }

    @Override
    public void updateLeave(Long leaveId, LeaveRequestDTO leaveRequestDTO) {
        validateLeaveRequest(leaveRequestDTO);

        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new LeaveException("Leave not found"));

        leaveAttributesMapper.updateEntity(leave, leaveRequestDTO);
        validateLeaveDateRange(leave);
        leaveRepository.save(leave);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadAttachment(Long leaveId) {
        String userDn = getCurrentUserDn();
        EmployeeResponse user = fetchEmployee(userDn);

        return leaveRepository.findById(leaveId)
                .filter(leave -> isHR(user) || isSameDepartment(user, leave.getUserDn()))
                .map(Leave::getAttachment)
                .orElseThrow(() -> new LeaveException("Leave not found or access denied"));
    }

    @Override
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void addMonthlyLeaveForAllEmployees() {
        leaveBalanceRepository.findAll().forEach(leaveBalance -> {
            leaveBalance.addMonthlyLeave();
            leaveBalanceRepository.save(leaveBalance);
        });
    }

    private void validateLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO == null) {
            throw new LeaveException("Leave request cannot be null");
        }
        if (leaveRequestDTO.startDate() == null || leaveRequestDTO.endDate() == null) {
            throw new LeaveException("Leave dates cannot be null");
        }
        if (leaveRequestDTO.startDate().isAfter(leaveRequestDTO.endDate())) {
            throw new LeaveException("Start date must be before end date");
        }
    }

    private EmployeeResponse fetchEmployee(String userDn) {
        try {
            return employeeClient.getEmployeeByDn(userDn);
        } catch (RuntimeException e) {
            log.error("Failed to fetch employee with DN: {}", userDn, e);
            throw new UserException("User not found");
        }
    }

    private EmployeeResponse fetchManager(String department) {
        try {
            return employeeClient.getManagerByDepartment(department);
        } catch (RuntimeException e) {
            log.error("Failed to fetch manager for department: {}", department, e);
            throw new UserException("Manager not found");
        }
    }

    private void processAttachment(Leave leave, MultipartFile attachment) {
        if (attachment != null && !attachment.isEmpty()) {
            try {
                leave.setAttachment(attachment.getBytes());
            } catch (IOException e) {
                log.error("Failed to process attachment", e);
                throw new FileUploadException("Failed to upload attachment");
            }
        }
    }

    private String getCurrentUserDn() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void validateLeaveDateRange(Leave leave) {
        leaveRepository.findAllByUserDn(leave.getUserDn()).forEach(existingLeave -> {
            if (isOverlappingLeave(existingLeave, leave)) {
                throw new TimeRangeException("Leave date range is overlapping with existing leave");
            }
        });
    }

    private boolean isOverlappingLeave(Leave existingLeave, Leave newLeave) {
        return (existingLeave.getStatus() == EStatus.APPROUVÉE || existingLeave.getStatus() == EStatus.EN_ATTENTE) &&
                newLeave.getStartDate().isBefore(existingLeave.getEndDate()) &&
                newLeave.getEndDate().isAfter(existingLeave.getStartDate());
    }

    private void updateLeaveBalance(Leave leave) {
        LeaveBalance leaveBalance = leaveBalanceRepository.findByUserDn(leave.getUserDn())
                .orElseThrow(() -> new LeaveBalanceException("Leave balance not found"));

        int daysBetween = (int) ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
        leaveBalance.setUsedLeave(leaveBalance.getUsedLeave() + daysBetween);
        leaveBalance.setRemainingLeave(leaveBalance.getTotalLeave() - leaveBalance.getUsedLeave());
        leaveBalanceRepository.save(leaveBalance);
    }

    private boolean isManager(EmployeeResponse user) {
        return "Manager".equals(user.role());
    }

    private boolean isHR(EmployeeResponse user) {
        return "HR".equals(user.department());
    }

    private boolean isSameDepartment(EmployeeResponse user, String otherUserDn) {
        try {
            EmployeeResponse otherUser = employeeClient.getEmployeeByDn(otherUserDn);
            return otherUser != null && user.department().equals(otherUser.department());
        } catch (RuntimeException e) {
            log.error("Failed to check department for user DN: {}", otherUserDn, e);
            return false;
        }
    }
}