package com.otbs.leave.service;

import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeaveAttributesMapper leaveAttributesMapper;
    private final UserClient userClient;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final AsyncProcessingService asyncProcessingService;

    private static final double WORKDAY_IN_MINUTES = 8*60;

    @Override
    public void applyLeave(LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        validateLeaveRequest(leaveRequestDTO);
        Leave leave = createLeaveEntity(leaveRequestDTO, attachment);
        leaveRepository.save(leave);
        UserResponse currentUser = getCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    currentUser.email(),
                    "Leave Request Submitted",
                    "Your leave request has been submitted successfully."
            );
        }
        UserResponse manager;
        if(currentUser.role().equals("Manager")) {
            manager = getDepartmentManager("HR");
        }
        else{
            manager = getDepartmentManager(currentUser.department());
        }

        if (manager.email() != null && !manager.email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    manager.email(),
                    "New Leave Request",
                    String.format("User %s has submitted a new leave request. Please review it.",
                            currentUser.username())
            );
        }
        asyncProcessingService.sendAppNotification(
                manager.id(),
                "New Leave Request",
                String.format("User %s has submitted a new leave request. Please review it.",
                        currentUser.username()),
                leave.getId(),
                "/leave"
        );
    }

    @Override
    public void updateLeave(Long leaveId, LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) {
        validateLeaveRequest(leaveRequestDTO);
        Leave leave = getLeaveForUpdate(leaveId);
        updateLeaveEntity(leave, leaveRequestDTO, attachment);
        leaveRepository.save(leave);
        UserResponse currentUser = getCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    currentUser.email(),
                    "Leave Request Updated",
                    "Your leave request has been updated successfully."
            );
        }
    }

    @Override
    public void cancelLeave(Long leaveId) {
        Leave leave = getLeaveForUpdate(leaveId);
        validatePendingStatus(leave);
        leave.setStatus(EStatus.CANCELLED);
        leaveRepository.save(leave);
        UserResponse currentUser = getCurrentUser();
        if (currentUser.email() != null && !currentUser.email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    currentUser.email(),
                    "Leave Request Cancelled",
                    "Your leave request has been cancelled successfully."
            );
        }
    }

    @Override
    public void approveLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        validateApprovalAuthorization(leave);
        processApprovalAndUpdateBalance(leave);
        UserResponse user = getUserByDn(leave.getUserDn());
        if (user.email() != null && !user.email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    user.email(),
                    "Leave Request Approved",
                    "Your leave request has been approved successfully."
            );
        }

        asyncProcessingService.sendAppNotification(
                user.id(),
                "Leave Request Approved",
                "Your leave request has been approved successfully.",
                leave.getId(),
                "/leave"
        );

    }

    @Override
    public void rejectLeave(Long leaveId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave not found"));
        validateRejectionAuthorization(leave);
        leave.setStatus(EStatus.REJECTED);
        leaveRepository.save(leave);
        UserResponse user = getUserByDn(leave.getUserDn());
        log .info("User email: {}", user.email());
        if (user.email() != null && !user.email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    user.email(),
                    "Leave Request Rejected",
                    "Your leave request has been rejected."
            );
        }
        asyncProcessingService.sendAppNotification(
                user.id(),
                "Leave Request Rejected",
                "Your leave request has been rejected.",
                leave.getId(),
                "/leave"
        );
    }

    @Override
    public List<LeaveResponseDTO> getAllRecievedLeavesRequests() {
        UserResponse currentUser = getCurrentUser();
        return switch (currentUser.role()) {
            case "Manager" -> getRecievedLeavesForManager(currentUser);
            case "HR" -> getRecievedLeavesForHR();
            case "HRD" -> getRecievedLeavesForHRDirector(currentUser);
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
        UserResponse user = getCurrentUser();
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

    @Override
    public boolean isUserOnLeave(String userDn, LocalDate date) {
        List<Leave> approvedLeaves = leaveRepository.findByUserDnAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
                userDn, date, date, EStatus.APPROVED);

        return !approvedLeaves.isEmpty();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    protected void addMonthlyLeaveForAllUsers() {
        leaveBalanceRepository.findAll().forEach(balance -> {
            balance.addMonthlyLeave();
            leaveBalanceRepository.save(balance);
            UserResponse user = getUserByDn(balance.getUserDn());
            if (user.email() != null && !user.email().isEmpty()) {
                asyncProcessingService.sendMailNotification(
                        user.email(),
                        "Monthly Leave Added",
                        "Your monthly leave has been added successfully."
                );
            }
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
                .orElseThrow(() -> new LeaveException("Leave not found or does not belong to the user"));
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
            throw new LeaveException("Only pending leaves can be modified");
        }
    }

    private void validateApprovalAuthorization(Leave leave) {
        UserResponse currentUser = getCurrentUser();
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
            throw new LeaveException("User is not authorized to approve this leave request.");
        }
    }

    private void validateRejectionAuthorization(Leave leave) {
        UserResponse currentUser = getCurrentUser();
        String leaveUserRole = getRoleByUserDn(leave.getUserDn());
        boolean isAuthorized = switch (leave.getDepartment()) {
            case "HR" -> "HRD".equals(currentUser.role());
            default -> switch (leaveUserRole) {
                case "Manager" -> "HRD".equals(currentUser.role());
                case "Employee" -> "Manager".equals(currentUser.role()) && currentUser.department().equals(leave.getDepartment());
                default -> false;
            };
        };
        if (!isAuthorized) {
            throw new LeaveException("User is not authorized to reject this leave request.");
        }
    }

    private List<LeaveResponseDTO> getLeaves(UserResponse currentUser) {
        return leaveRepository.findAllByUserDn(currentUser.id()).stream()
                .sorted((l1, l2) -> l2.getCreatedAt().compareTo(l1.getCreatedAt()))
                .map(leaveAttributesMapper::toDto)
                .toList();
    }

    private List<LeaveResponseDTO> getRecievedLeavesForManager(UserResponse currentUser) {
        return leaveRepository.findAll().stream()
                .filter(leave -> "HR".equals(currentUser.department())? List.of("Manager", "HR").contains(getRoleByUserDn(leave.getUserDn())): "Employee".equals(getRoleByUserDn(leave.getUserDn())) && leave.getDepartment().equals(currentUser.department()))
                .sorted((l1, l2) -> l2.getCreatedAt().compareTo(l1.getCreatedAt()))
                .map(leaveAttributesMapper::toDto)
                .toList();
    }

    private List<LeaveResponseDTO> getRecievedLeavesForHRDirector(UserResponse currentUser) {
        return leaveRepository.findAll().stream()
                .filter(leave -> "HR".equals(currentUser.department()) || "Manager".equals(getRoleByUserDn(leave.getUserDn())))
                .sorted((l1, l2) -> l2.getCreatedAt().compareTo(l1.getCreatedAt()))
                .map(leaveAttributesMapper::toDto)
                .toList();
    }

    private List<LeaveResponseDTO> getRecievedLeavesForHR() {
        return leaveRepository.findAll().stream()
                .sorted((l1, l2) -> l2.getCreatedAt().compareTo(l1.getCreatedAt()))
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
                throw new LeaveException("Leave date range is overlapping with existing leave");
            }
        });
    }

    private boolean isOverlappingLeave(Leave existingLeave, Leave newLeave) {
        return List.of(EStatus.APPROVED, EStatus.PENDING).contains(existingLeave.getStatus())
                && newLeave.getStartDate().isBefore(existingLeave.getEndDate().plusDays(1))
                && newLeave.getEndDate().plusDays(1).isAfter(existingLeave.getStartDate());
    }

    private UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserResponse userResponse) {
            return userResponse;
        }
        throw new LeaveException( String.format("Current user is not authenticated or does not have a valid user response: %s", principal));
    }

    private String getRoleByUserDn(String userDn) {
        UserResponse userResponse = userClient.getUserByDn(userDn);
        return userResponse != null ? userResponse.role() : null;
    }

    private UserResponse getUserByDn(String userDn) {
        UserResponse userResponse = userClient.getUserByDn(userDn);
        if (userResponse == null) {
            throw new UsernameNotFoundException( String.format("User not found with DN: %s", userDn));
        }
        return userResponse;
    }

    private UserResponse getDepartmentManager(String department) {
        UserResponse manager = userClient.getManagerByDepartment(department);
        if (manager == null) {
            throw new UserException( String.format("Manager not found for department: %s", department));
        }
        return manager;
    }
}