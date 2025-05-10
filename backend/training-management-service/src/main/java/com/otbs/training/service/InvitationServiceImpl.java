package com.otbs.training.service;

import com.otbs.common.event.Event;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.training.dto.InvitationResponseDTO;
import com.otbs.training.exception.InvitationException;
import com.otbs.training.mapper.InvitationMapper;
import com.otbs.training.model.EStatus;
import com.otbs.training.model.Invitation;
import com.otbs.training.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final TrainingNotificationService trainingNotificationService;
    private final EmployeeClient employeeClient;
    private final TrainingNotificationEventService notificationEventService;

    @Override
    @Transactional
    public void confirmInvitation(Long invitationId) {
        String employeeId = getCurrentUserId();

        // Find and update invitation
        invitationRepository.findByEmployeeIdAndTrainingId(employeeId, invitationId)
                .ifPresentOrElse(invitation -> {
                    invitation.setStatus(EStatus.CONFIRMED);
                    invitationRepository.save(invitation);

                    // Asynchronously fetch employee details and send notification
                    fetchEmployeeAndNotifyAsync(employeeId, invitationId);
                }, () -> {
                    throw new InvitationException("Invitation not found for confirmation");
                });
        sendAsyncEventNotifications(invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException("Invitation not found")), "CONFIRMED_INVITATION");
    }

    @Override
    public List<InvitationResponseDTO> getAllInvitations() {
        String employeeId = getCurrentUserId();
        return invitationRepository.findByEmployeeId(employeeId)
                .stream()
                .map(invitationMapper::toResponseDTO)
                .toList();
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Async
    protected void fetchEmployeeAndNotifyAsync(String employeeId, Long invitationId) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return employeeClient.getEmployeeByDn(employeeId);
            } catch (RuntimeException e) {
                log.error("Error fetching employee details for ID {}: {}", employeeId, e.getMessage());
                throw new InvitationException("Error fetching employee details");
            }
        }).thenAccept(employee -> {
            trainingNotificationService.sendMailNotification(
                    employee.email(),
                    "Invitation Confirmation",
                    "Your invitation for training ID " + invitationId + " has been confirmed successfully."
            );
        }).exceptionally(throwable -> {
            log.error("Failed to send notification for employee {}: {}", employeeId, throwable.getMessage());
            return null;
        });
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncEventNotifications(Invitation invitation, String eventType) {
        InvitationResponseDTO invitationResponseDTO = invitationMapper.toResponseDTO(invitation);
        Map<String, Object> payload = new HashMap<>();
        payload.put("invitation", invitationResponseDTO);
        CompletableFuture<Void> eventNotification = CompletableFuture.runAsync(() ->
                notificationEventService.sendEventNotification(
                        new Event(
                                eventType,
                                invitation.getId().toString(),
                                "INVITATION",
                                payload
                        )
                ));

        CompletableFuture.allOf(eventNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send event notifications for invitation ID {}: {}",
                                invitation.getId(), throwable.getMessage());
                    } else {
                        log.debug("Event notifications sent successfully for invitation ID {}", invitation.getId());
                    }
                });
    }
}