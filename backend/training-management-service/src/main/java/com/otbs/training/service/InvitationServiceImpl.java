package com.otbs.training.service;

import com.otbs.feign.client.user.UserClient;
import com.otbs.training.dto.InvitationResponseDTO;
import com.otbs.training.exception.InvitationException;
import com.otbs.training.mapper.InvitationMapper;
import com.otbs.training.model.EStatus;
import com.otbs.training.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final TrainingNotificationService trainingNotificationService;
    private final UserClient userClient;

    @Override
    @Transactional
    public void confirmInvitation(Long invitationId) {
        String userId = getCurrentUserId();

        // Find and update invitation
        invitationRepository.findByUserIdAndTrainingId(userId, invitationId)
                .ifPresentOrElse(invitation -> {
                    invitation.setStatus(EStatus.CONFIRMED);
                    invitationRepository.save(invitation);
                    log.info("Invitation with ID {} confirmed for user {}", invitationId, userId);
                    // Asynchronously fetch user details and send notification
                    fetchUserAndNotifyAsync(userId, invitationId);
                }, () -> {
                    throw new InvitationException("Invitation not found for confirmation");
                });
    }

    @Override
    public List<InvitationResponseDTO> getAllInvitations() {
        String userId = getCurrentUserId();
        return invitationRepository.findByUserId(userId)
                .stream()
                .map(invitationMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<InvitationResponseDTO> getAllInvitationsByTrainingId(Long trainingId) {
        return invitationRepository.findAllByTrainingId(trainingId)
                .stream()
                .map(invitationMapper::toResponseDTO)
                .toList();
    }


    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Async
    protected void fetchUserAndNotifyAsync(String userId, Long invitationId) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return userClient.getUserByDn(userId);
            } catch (RuntimeException e) {
                log.error("Error fetching user details for ID {}: {}", userId, e.getMessage());
                throw new InvitationException("Error fetching user details");
            }
        }).thenAccept(user -> {
            trainingNotificationService.sendMailNotification(
                    user.email(),
                    "Invitation Confirmation",
                    "Your invitation for training ID " + invitationId + " has been confirmed successfully."
            );
        }).exceptionally(throwable -> {
            log.error("Failed to send notification for user {}: {}", userId, throwable.getMessage());
            return null;
        });
    }

}