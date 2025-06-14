package com.otbs.training.service;

import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
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
    private final AsyncProcessingService asyncProcessingService;

    @Override
    @Transactional
    public void confirmInvitation(Long invitationId) {
        String userId = getCurrentUser().id();

        // Find and update invitation
        invitationRepository.findByUserIdAndTrainingId(userId, invitationId)
                .ifPresentOrElse(invitation -> {
                    invitation.setStatus(EStatus.CONFIRMED);
                    invitationRepository.save(invitation);
                    log.info("Invitation with ID {} confirmed for user {}", invitationId, userId);
                    asyncProcessingService.sendAppNotification(
                            invitation.getTraining().getCreatedBy(),
                            "Invitation Confirmed",
                            String.format("User %s has confirmed the invitation for training %s", getCurrentUser().firstName()+" "+getCurrentUser().lastName(), invitation.getTraining().getTitle()),
                            invitation.getTraining().getId(),
                            "/trainings"
                    );
                    if(getCurrentUser().email() != null && !getCurrentUser().email().isEmpty()) {
                        asyncProcessingService.sendMailNotification(
                                getCurrentUser().email(),
                                "Invitation Confirmed",
                                String.format("You have successfully confirmed the invitation for training %s", invitation.getTraining().getTitle())
                        );
                    }
                }, () -> {
                    throw new InvitationException("Invitation not found for confirmation");
                });
    }

    @Override
    public List<InvitationResponseDTO> getAllInvitations() {
        String userId = getCurrentUser().id();
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


    private UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserResponse userResponse) {
            return userResponse;
        }
        throw new InvitationException( String.format("Current user is not authenticated or does not have a valid user response: %s", principal));
    }



}