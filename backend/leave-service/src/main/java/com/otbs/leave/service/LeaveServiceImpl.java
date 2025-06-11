package com.otbs.leave.service;

import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.employee.dto.EmployeeResponse;
import com.otbs.leave.dto.LeaveRequestDTO;
import com.otbs.leave.dto.LeaveResponseDTO;
import com.otbs.leave.exception.*;
import com.otbs.leave.mapper.LeaveAttributesMapper;
import com.otbs.leave.model.*;
import com.otbs.leave.repository.LeaveBalanceRepository;
import com.otbs.leave.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeaveAttributesMapper leaveAttributesMapper;
    private final EmployeeClient employeeClient;
    private final LeaveBalanceRepository leaveBalanceRepository;

    private static final double WORKDAY_IN_MINUTES = 8*60;

    @Override
    public void applyLeave(LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        validateLeaveRequest(leaveRequestDTO);
        Leave leave = createLeaveEntity(leaveRequestDTO, attachment);
        leaveRepository.save(leave);
    }

    @Override
    public void updateLeave(Long leaveId, LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        validateLeaveRequest(leaveRequestDTO);
        Leave leave = getLeaveForUpdate(leaveId);
        updateLeaveEntity(leave, leaveRequestDTO, attachment);
        leaveRepository.save(leave);
    }

    @Override
    public void cancelLeave(Long leaveId) {
        Leave leave = getLeaveForUpdate(leaveId);
        validatePendingStatus(leave);
        leave.setStatus(EStatus.CANCELLED);
        leaveRepository.save(leave);
    }

    @Override
    public void approveLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        validateApprovalAuthorization(leave);
        processApprovalAndUpdateBalance(leave);
    }

    @Override
    public void rejectLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        validateRejectionAuthorization(leave);
        leave.setStatus(EStatus.REJECTED);
        leaveRepository.save(leave);
    }

    @Override
    public List<LeaveResponseDTO> getAllRecievedLeavesRequests() {
        EmployeeResponse currentUser = getCurrentUser();
        return switch (currentUser.role()) {
            case "Manager" -> getRecievedLeavesForManager(currentUser);
            case "HR" -> getRecievedLeavesForHR();
            default -> throw new IllegalStateException("Unexpected role");
        };
    }

    @Override
    public List<LeaveResponseDTO> getAllSentLeavesRequests() {
        return getLeaves(getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadAttachment(Long leaveId) {
        EmployeeResponse user = getCurrentUser();
        return leaveRepository.findById(leaveId)
                .filter(leave -> leave.getDepartment().equals(user.department()) || "HR".equals(user.role()))
                .map(Leave::getAttachment)
                .orElseThrow(() -> new LeaveException("Leave not found or access denied"));
    }

    @Override
    public LeaveBalance getLeaveBalance() {
        return leaveBalanceRepository.findByUserDn(getCurrentUser().id())
                .orElseGet(() -> leaveBalanceRepository.save(new LeaveBalance(getCurrentUser().id(), 0.0, 0.0, 0.0)));
    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    protected void addMonthlyLeaveForAllEmployees() {
        leaveBalanceRepository.findAll().forEach(balance -> {
            balance.addMonthlyLeave();
            leaveBalanceRepository.save(balance);
        });
    }

    private Leave createLeaveEntity(LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        Leave leave = leaveAttributesMapper.toEntity(leaveRequestDTO);
        leave.setUserDn(getCurrentUser().id());
        leave.setDepartment(getCurrentUser().department());
        processAttachment(leave, attachment);
        validateLeaveDateRange(leave);
        return leave;
    }

    private Leave getLeaveForUpdate(Long leaveId) {
        return leaveRepository.findByIdAndUserDn(leaveId, getCurrentUser().id())
                .orElseThrow(() -> new IllegalArgumentException("Leave not found or does not belong to the user"));
    }

    private void updateLeaveEntity(Leave leave, LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        validatePendingStatus(leave);
        leaveAttributesMapper.updateEntity(leave, leaveRequestDTO);
        processAttachment(leave, attachment);
        validateLeaveDateRange(leave);
        leave.setDepartment(getCurrentUser().department());
    }

    private void validatePendingStatus(Leave leave) {
        if (!EStatus.PENDING.equals(leave.getStatus())) {
            throw new IllegalArgumentException("Only pending leaves can be modified");
        }
    }

    private void validateApprovalAuthorization(Leave leave) {
        EmployeeResponse currentUser = getCurrentUser();
        String leaveUserRole = getRoleByUserDn(leave.getUserDn());
        boolean isAuthorized = switch (leave.getDepartment()) {
            case "HR" -> "Manager".equals(currentUser.role()) && "HR".equals(currentUser.department());
            default -> switch (leaveUserRole) {
                case "Manager" -> "Manager".equals(currentUser.role()) && "HR".equals(currentUser.department());
                case "Employee" -> "Manager".equals(currentUser.role()) && currentUser.department().equals(leave.getDepartment());
                default -> false;
            };
        };
        if (!isAuthorized) {
            throw new IllegalArgumentException("User is not authorized to approve this leave request.");
        }
    }

    private void validateRejectionAuthorization(Leave leave) {
        EmployeeResponse currentUser = getCurrentUser();
        String leaveUserRole = getRoleByUserDn(leave.getUserDn());
        boolean isAuthorized = switch (leave.getDepartment()) {
            case "HR" -> "Manager".equals(currentUser.role()) && "HR".equals(currentUser.department());
            default -> switch (leaveUserRole) {
                case "Manager" -> "Manager".equals(currentUser.role()) && "HR".equals(currentUser.department());
                case "Employee" -> "Manager".equals(currentUser.role()) && currentUser.department().equals(leave.getDepartment());
                default -> false;
            };
        };
        if (!isAuthorized) {
            throw new IllegalArgumentException("User is not authorized to reject this leave request.");
        }
    }

    private List<LeaveResponseDTO> getLeaves(EmployeeResponse currentUser) {
        return leaveRepository.findAllByUserDn(currentUser.id()).stream()
                .map(leaveAttributesMapper::toDto)
                .toList();
    }

    private List<LeaveResponseDTO> getRecievedLeavesForManager(EmployeeResponse currentUser) {
        return leaveRepository.findAll().stream()
                .filter(leave -> "HR".equals(currentUser.department())? List.of("Manager", "HR").contains(getRoleByUserDn(leave.getUserDn())): "Employee".equals(getRoleByUserDn(leave.getUserDn())) && leave.getDepartment().equals(currentUser.department()))
                .map(leaveAttributesMapper::toDto)
                .toList();
    }

    private List<LeaveResponseDTO> getRecievedLeavesForHR() {
        return leaveRepository.findAll().stream()
                .map(leaveAttributesMapper::toDto)
                .toList();
    }

    private void processApprovalAndUpdateBalance(Leave leave) {
        validatePendingStatus(leave);
        leave.setStatus(EStatus.APPROVED);
        leaveRepository.save(leave);

        LeaveBalance leaveBalance = leaveBalanceRepository.findByUserDn(leave.getUserDn())
                .orElseThrow(() -> new LeaveBalanceException("Leave balance not found for user: " + leave.getUserDn()));

        double leaveDurationInDays = calculateLeaveDuration(leave);
        leaveBalance.setUsedLeave(leaveBalance.getUsedLeave() + leaveDurationInDays);
        leaveBalance.setRemainingLeave(leaveBalance.getTotalLeave() - leaveBalance.getUsedLeave());
        leaveBalanceRepository.save(leaveBalance);
    }

    private double calculateLeaveDuration(Leave leave) {
        if (leave.getLeaveType() == ELeaveType.AUTORISATION) {
            if (leave.getStartTime() == null || leave.getEndTime() == null) {
                throw new LeaveException("Cannot approve AUTORISATION leave without start and end times.");
            }
            return ChronoUnit.MINUTES.between(leave.getStartTime(), leave.getEndTime()) / WORKDAY_IN_MINUTES;
        }
        return ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()) + 1;
    }

    private void validateLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        if (leaveRequestDTO == null || leaveRequestDTO.startDate() == null || leaveRequestDTO.endDate() == null) {
            throw new LeaveException("Leave request and dates cannot be null");
        }
        if (leaveRequestDTO.startDate().isAfter(leaveRequestDTO.endDate())) {
            throw new LeaveException("Start date must be before end date");
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

    private void validateLeaveDateRange(Leave leave) {
        leaveRepository.findAllByUserDn(leave.getUserDn()).forEach(existingLeave -> {
            if (!existingLeave.getId().equals(leave.getId()) && isOverlappingLeave(existingLeave, leave)) {
                throw new TimeRangeException("Leave date range is overlapping with existing leave");
            }
        });
    }

    private boolean isOverlappingLeave(Leave existingLeave, Leave newLeave) {
        return List.of(EStatus.APPROVED, EStatus.PENDING).contains(existingLeave.getStatus())
                && newLeave.getStartDate().isBefore(existingLeave.getEndDate().plusDays(1))
                && newLeave.getEndDate().plusDays(1).isAfter(existingLeave.getStartDate());
    }

    private EmployeeResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof EmployeeResponse employeeResponse) {
            return employeeResponse;
        }
        throw new IllegalStateException("Current user is not authenticated or does not have the expected type");
    }

    private String getRoleByUserDn(String userDn) {
        EmployeeResponse employeeResponse = employeeClient.getEmployeeByDn(userDn);
        return employeeResponse != null ? employeeResponse.role() : null;
    }
}