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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private static final String HR_ROLE = "HR";
    private static final String MANAGER_ROLE = "Manager";
    private static final String EMPLOYEE_ROLE = "Employee";
    private static final String HRD_ROLE = "HRD";
    private final AsyncProcessingService asyncProcessingService;

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final UserClient userClient;

    @Override
    @Transactional
    public void createTraining(TrainingRequestDTO request) {
        UserResponse manager = getCurrentUser();

        validateDateRange(request);

        Training training = trainingMapper.toEntity(request);
        training.setCreatedBy(manager.id());
        training.setDepartment(manager.department());
        Training savedTraining = trainingRepository.save(training);
        List<Invitation> invitations = new ArrayList<>();
        userClient.getAllUsers()
                .stream()
                .filter(user -> user.department().equals(manager.department()) && !user.id().equals(manager.id()))
                .forEach(user -> {
                    Invitation invitation = new Invitation();
                    invitation.setUserId(user.id());
                    invitation.setTraining(savedTraining);
                    invitation.setStatus(EStatus.PENDING);
                    invitation.setTraining(savedTraining);
                    invitations.add(invitation);
                    asyncProcessingService.sendAppNotification(
                            user.id(),
                            "New Training Invitation",
                            String.format("You have been invited to the training: %s", savedTraining.getTitle()),
                            savedTraining.getId(),
                            "/trainings"
                    );
                    if (user.email() != null && !user.email().isEmpty()) {
                        asyncProcessingService.sendMailNotification(
                                user.email(),
                                "New Training Invitation",
                                String.format("You have been invited to the training: %s", savedTraining.getTitle())
                        );
                    }
                });
        savedTraining.setInvitations(invitations);
        trainingRepository.save(savedTraining);
    }

    @Override
    @Transactional
    public void updateTraining(TrainingRequestDTO request, Long trainingId) {
        String managerId = getCurrentUser().id();
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
        String managerId = getCurrentUser().id();
        Training training = findTrainingForManager(managerId, trainingId);
        trainingRepository.delete(training);
    }

    @Override
    public TrainingResponseDTO getTrainingById(Long trainingId) {
        UserResponse user = getCurrentUser();

        if (HR_ROLE.equals(user.role()) || HRD_ROLE.equals(user.role())) {
            return trainingRepository.findById(trainingId)
                    .map(trainingMapper::toResponseDTO)
                    .orElseThrow(() -> new TrainingException("Training not found"));
        } else if (MANAGER_ROLE.equals(user.role())) {
            return trainingMapper.toResponseDTO(findTrainingForManager(user.id(), trainingId));
        } else if (EMPLOYEE_ROLE.equals(user.role())) {
            return trainingRepository.findByIdAndInvitations_UserId(trainingId, user.id()).stream()
                    .map(
                            training -> {
                                TrainingResponseDTO responseDTO = trainingMapper.toResponseDTO(training);
                                responseDTO.setIsConfirmed(
                                        training.getInvitations().stream()
                                                .anyMatch(invitation -> invitation.getUserId().equals(user.id()) && invitation.getStatus() == EStatus.CONFIRMED)
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
        UserResponse user = getCurrentUser();

        if (HR_ROLE.equals(user.role()) || HRD_ROLE.equals(user.role())) {
            return trainingRepository.findAll().stream()
                    .map(trainingMapper::toResponseDTO)
                    .toList();
        } else if (MANAGER_ROLE.equals(user.role())) {
            return trainingRepository.findByCreatedBy(user.id()).stream()
                    .map(trainingMapper::toResponseDTO)
                    .toList();
        } else if (EMPLOYEE_ROLE.equals(user.role())) {
            return trainingRepository.findByInvitations_UserId(user.id()).stream()
                    .peek(training -> training.setInvitations(training.getInvitations().stream()
                            .filter(invitation -> invitation.getUserId().equals(user.id()))
                            .toList()))
                    .map(training -> {
                        TrainingResponseDTO responseDTO = trainingMapper.toResponseDTO(training);
                        responseDTO.setIsConfirmed(
                                training.getInvitations().stream()
                                        .anyMatch(invitation -> invitation.getUserId().equals(user.id()) && invitation.getStatus() == EStatus.CONFIRMED)
                        );
                        return responseDTO;
                    })
                    .toList();
        }

        throw new TrainingException("Not authorized to view trainings");
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
    private UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserResponse userResponse) {
            return userResponse;
        }
        throw new TrainingException( String.format("Current user is not authenticated or does not have a valid user response: %s", principal));
    }

}