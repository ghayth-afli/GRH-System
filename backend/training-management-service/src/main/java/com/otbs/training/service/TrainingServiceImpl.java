package com.otbs.training.service;

import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
import com.otbs.training.dto.TrainingRequestDTO;
import com.otbs.training.dto.TrainingResponseDTO;
import com.otbs.training.exception.TrainingException;
import com.otbs.training.mapper.TrainingMapper;
import com.otbs.training.model.EStatus;
import com.otbs.training.model.Invitation;
import com.otbs.training.model.Training;
import com.otbs.training.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private static final String HR_ROLE = "HR";
    private static final String MANAGER_ROLE = "Manager";
    private static final String EMPLOYEE_ROLE = "Employee";
    private static final String HRD_ROLE = "HRD";

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final UserClient userClient;
    private final TrainingNotificationService trainingNotificationService;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public void createTraining(TrainingRequestDTO request) {
        String managerId = getCurrentUserId();
        UserResponse manager = fetchUser(managerId);

        validateDateRange(request);

        Training training = trainingMapper.toEntity(request);
        training.setCreatedBy(managerId);
        training.setDepartment(manager.department());

        Training savedTraining = trainingRepository.save(training);

        fetchDepartmentUsersAndNotifyAsync(manager, savedTraining, request);
    }

    @Override
    @Transactional
    public void updateTraining(TrainingRequestDTO request, Long trainingId) {
        String managerId = getCurrentUserId();
        Training training = findTrainingForManager(managerId, trainingId);

        validateDateRange(request);

        training.setTitle(request.title());
        training.setDescription(request.description());
        training.setStartDate(request.startDate());
        training.setEndDate(request.endDate());

        trainingRepository.save(training);
    }

    @Override
    @Transactional
    public void deleteTraining(Long trainingId) {
        String managerId = getCurrentUserId();
        Training training = findTrainingForManager(managerId, trainingId);
        trainingRepository.delete(training);
    }

    @Override
    public TrainingResponseDTO getTrainingById(Long trainingId) {
        String userId = getCurrentUserId();
        UserResponse user = fetchUser(userId);

        if (HR_ROLE.equals(user.role()) || HRD_ROLE.equals(user.role())) {
            return trainingRepository.findById(trainingId)
                    .map(trainingMapper::toResponseDTO)
                    .orElseThrow(() -> new TrainingException("Training not found"));
        } else if (MANAGER_ROLE.equals(user.role())) {
            return trainingMapper.toResponseDTO(findTrainingForManager(userId, trainingId));
        } else if (EMPLOYEE_ROLE.equals(user.role())) {
            return trainingRepository.findByIdAndInvitations_UserId(trainingId, userId).stream()
                    .map(
                            training -> {
                                TrainingResponseDTO responseDTO = trainingMapper.toResponseDTO(training);
                                responseDTO.setIsConfirmed(
                                        training.getInvitations().stream()
                                                .anyMatch(invitation -> invitation.getUserId().equals(userId) && invitation.getStatus() == EStatus.CONFIRMED)
                                );
                                return responseDTO;
                            }
                    )
                    .findFirst()
                    .orElseThrow(() -> new TrainingException("Training not found or not authorized"));
        }

        throw new TrainingException("Not authorized to view this training");
    }

    @Override
    public List<TrainingResponseDTO> getAllTrainings() {
        String userId = getCurrentUserId();
        UserResponse user = fetchUser(userId);

        if (HR_ROLE.equals(user.role()) || HRD_ROLE.equals(user.role())) {
            return trainingRepository.findAll().stream()
                    .map(trainingMapper::toResponseDTO)
                    .toList();
        } else if (MANAGER_ROLE.equals(user.role())) {
            return trainingRepository.findByCreatedBy(userId).stream()
                    .map(trainingMapper::toResponseDTO)
                    .toList();
        } else if (EMPLOYEE_ROLE.equals(user.role())) {
            return trainingRepository.findByInvitations_UserId(userId).stream()
                    .peek(training -> training.setInvitations(training.getInvitations().stream()
                            .filter(invitation -> invitation.getUserId().equals(userId))
                            .toList()))
                    .map(training -> {
                        TrainingResponseDTO responseDTO = trainingMapper.toResponseDTO(training);
                        responseDTO.setIsConfirmed(
                                training.getInvitations().stream()
                                        .anyMatch(invitation -> invitation.getUserId().equals(userId) && invitation.getStatus() == EStatus.CONFIRMED)
                        );
                        return responseDTO;
                    })
                    .toList();
        }

        throw new TrainingException("Not authorized to view trainings");
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Training findTrainingForManager(String managerId, Long trainingId) {
        return trainingRepository.findByCreatedByAndId(managerId, trainingId)
                .orElseThrow(() -> new TrainingException("Training not found or not authorized"));
    }

    private void validateDateRange(TrainingRequestDTO request) {
        if (request.startDate() == null || request.endDate() == null) {
            throw new TrainingException("Start and end dates cannot be null");
        }
        if (request.startDate().isAfter(request.endDate())) {
            throw new TrainingException("Start date must be before end date");
        }
    }

    private UserResponse fetchUser(String userId) {
        try {
            return userClient.getUserByDn(userId);
        } catch (RuntimeException e) {
            log.error("Error fetching user details for ID {}: {}", userId, e.getMessage());
            throw new TrainingException("Error fetching user details");
        }
    }

    @Async
    protected void fetchDepartmentUsersAndNotifyAsync(UserResponse manager, Training savedTraining, TrainingRequestDTO request) {
        CompletableFuture.supplyAsync(userClient::getAllUsers)
                .thenApply(users -> users.stream()
                        .filter(emp -> emp.department().equals(manager.department()))
                        .filter(emp -> !emp.id().equals(manager.id()))
                        .toList())
                .thenAccept(departmentUsers -> {
                    List<Invitation> invitations = departmentUsers.stream()
                            .map(emp -> {
                                Invitation invitation = new Invitation();
                                invitation.setUserId(emp.id());
                                invitation.setStatus(EStatus.PENDING);
                                invitation.setTraining(savedTraining);

                                String message = String.format("You have been invited to a training: %s", request.title());
                                CompletableFuture.allOf(
                                        CompletableFuture.runAsync(() -> trainingNotificationService.sendTrainingNotification(
                                                emp.id(), "Training Invitation", message, savedTraining.getId())),
                                        CompletableFuture.runAsync(() -> trainingNotificationService.sendMailNotification(
                                                emp.email(), "Training Invitation", message))
                                ).exceptionally(throwable -> {
                                    log.error("Failed to send notification to user {}: {}", emp.id(), throwable.getMessage());
                                    return null;
                                });

                                return invitation;
                            })
                            .toList();

                    try {
                        transactionTemplate.execute(_ -> {
                            savedTraining.setInvitations(invitations);
                            trainingRepository.save(savedTraining);
                            return null;
                        });
                    } catch (Exception e) {
                        log.error("Failed to save invitations for training {}: {}", savedTraining.getId(), e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Failed to fetch users for training {}: {}", savedTraining.getId(), throwable.getMessage());
                    return null;
                });
    }

}