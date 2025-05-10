package com.otbs.training.service;

import com.otbs.common.event.Event;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.employee.dto.EmployeeResponse;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private static final String HR_ROLE = "HR";
    private static final String MANAGER_ROLE = "Manager";
    private static final String EMPLOYEE_ROLE = "Employee";

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final EmployeeClient employeeClient;
    private final TrainingNotificationService trainingNotificationService;
    private final TransactionTemplate transactionTemplate;
    private final TrainingNotificationEventService notificationEventService;

    @Override
    @Transactional
    public void createTraining(TrainingRequestDTO request) {
        String managerId = getCurrentUserId();
        EmployeeResponse manager = fetchEmployee(managerId);

        validateDateRange(request);

        Training training = trainingMapper.toEntity(request);
        training.setCreatedBy(managerId);
        training.setDepartment(manager.department());

        Training savedTraining = trainingRepository.save(training);

        fetchDepartmentEmployeesAndNotifyAsync(manager, savedTraining, request);
        sendAsyncEventNotifications(savedTraining, "CREATED_TRAINING");
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
        sendAsyncEventNotifications(training, "UPDATED_TRAINING");
    }

    @Override
    @Transactional
    public void deleteTraining(Long trainingId) {
        String managerId = getCurrentUserId();
        Training training = findTrainingForManager(managerId, trainingId);
        trainingRepository.delete(training);
        sendAsyncEventNotifications(training, "DELETED_TRAINING");
    }

    @Override
    public TrainingResponseDTO getTrainingById(Long trainingId) {
        String userId = getCurrentUserId();
        EmployeeResponse user = fetchEmployee(userId);

        if (HR_ROLE.equals(user.role())) {
            return trainingRepository.findById(trainingId)
                    .map(trainingMapper::toResponseDTO)
                    .orElseThrow(() -> new TrainingException("Training not found"));
        } else if (MANAGER_ROLE.equals(user.role())) {
            return trainingMapper.toResponseDTO(findTrainingForManager(userId, trainingId));
        } else if (EMPLOYEE_ROLE.equals(user.role())) {
            return trainingRepository.findByIdAndInvitations_EmployeeId(trainingId, userId).stream()
                    .map(trainingMapper::toResponseDTO)
                    .findFirst()
                    .orElseThrow(() -> new TrainingException("Training not found or not authorized"));
        }

        throw new TrainingException("Not authorized to view this training");
    }

    @Override
    public List<TrainingResponseDTO> getAllTrainings() {
        String userId = getCurrentUserId();
        EmployeeResponse user = fetchEmployee(userId);

        if (HR_ROLE.equals(user.role())) {
            return trainingRepository.findAll().stream()
                    .map(trainingMapper::toResponseDTO)
                    .toList();
        } else if (MANAGER_ROLE.equals(user.role())) {
            return trainingRepository.findByCreatedBy(userId).stream()
                    .map(trainingMapper::toResponseDTO)
                    .toList();
        } else if (EMPLOYEE_ROLE.equals(user.role())) {
            return trainingRepository.findByInvitations_EmployeeId(userId).stream()
                    .peek(training -> training.setInvitations(training.getInvitations().stream()
                            .filter(invitation -> invitation.getEmployeeId().equals(userId))
                            .toList()))
                    .map(trainingMapper::toResponseDTO)
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

    private EmployeeResponse fetchEmployee(String employeeId) {
        try {
            return employeeClient.getEmployeeByDn(employeeId);
        } catch (RuntimeException e) {
            log.error("Error fetching employee details for ID {}: {}", employeeId, e.getMessage());
            throw new TrainingException("Error fetching employee details");
        }
    }

    @Async
    protected void fetchDepartmentEmployeesAndNotifyAsync(EmployeeResponse manager, Training savedTraining, TrainingRequestDTO request) {
        CompletableFuture.supplyAsync(employeeClient::getAllEmployees)
                .thenApply(employees -> employees.stream()
                        .filter(emp -> emp.department().equals(manager.department()))
                        .filter(emp -> !emp.id().equals(manager.id()))
                        .toList())
                .thenAccept(departmentEmployees -> {
                    List<Invitation> invitations = departmentEmployees.stream()
                            .map(emp -> {
                                Invitation invitation = new Invitation();
                                invitation.setEmployeeId(emp.id());
                                invitation.setStatus(EStatus.PENDING);
                                invitation.setTraining(savedTraining);

                                String message = String.format("You have been invited to a training: %s", request.title());
                                CompletableFuture.allOf(
                                        CompletableFuture.runAsync(() -> trainingNotificationService.sendTrainingNotification(
                                                emp.id(), "Training Invitation", message, savedTraining.getId())),
                                        CompletableFuture.runAsync(() -> trainingNotificationService.sendMailNotification(
                                                emp.email(), "Training Invitation", message))
                                ).exceptionally(throwable -> {
                                    log.error("Failed to send notification to employee {}: {}", emp.id(), throwable.getMessage());
                                    return null;
                                });

                                return invitation;
                            })
                            .toList();

                    // Save invitations with TransactionTemplate
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
                    log.error("Failed to fetch employees for training {}: {}", savedTraining.getId(), throwable.getMessage());
                    return null;
                });
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncEventNotifications(Training training, String eventType) {
        TrainingResponseDTO medicalVisitResponseDTO = trainingMapper.toResponseDTO(training);
        Map<String, Object> payload = new HashMap<>();
        payload.put("training", medicalVisitResponseDTO);
        CompletableFuture<Void> eventNotification = CompletableFuture.runAsync(() ->
                notificationEventService.sendEventNotification(
                        new Event(
                                eventType,
                                training.getId().toString(),
                                "TRAINING",
                                payload
                        )
                ));

        CompletableFuture.allOf(eventNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send event notifications for training ID {}: {}",
                                training.getId(), throwable.getMessage());
                    } else {
                        log.debug("Event notifications sent successfully for training ID {}", training.getId());
                    }
                });
    }
}