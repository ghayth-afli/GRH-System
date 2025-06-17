package com.otbs.medVisit.service;

import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.exception.AppointmentException;
import com.otbs.medVisit.exception.MedicalVisitException;
import com.otbs.medVisit.mapper.MedicalVisitMapper;
import com.otbs.medVisit.model.MedicalVisit;
import com.otbs.medVisit.repository.MedicalVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalVisitServiceImpl implements MedicalVisitService {

    private final MedicalVisitRepository medicalVisitRepository;
    private final MedicalVisitMapper medicalVisitMapper;
    private final AsyncProcessingService asyncProcessingService;
    private final UserClient userClient;


    @Override
    @Transactional
    public void createMedicalVisit(MedicalVisitRequestDTO medicalVisitRequestDTO) {
        // Validate request
        validateMedicalVisitRequest(medicalVisitRequestDTO);

        // Check for existing medical visit
        if (medicalVisitRepository.existsByDoctorNameAndVisitDate(
                medicalVisitRequestDTO.doctorName(), medicalVisitRequestDTO.visitDate())) {
            throw new MedicalVisitException("Medical visit already exists for the same doctor and date");
        }

        // Save medical visit
        MedicalVisit medVisit = medicalVisitMapper.toEntity(medicalVisitRequestDTO);
        medicalVisitRepository.save(medVisit);
        userClient.getAllUsers()
                .forEach(
                        user -> {
                            if (user.email() != null && !user.email().isBlank()) {
                                asyncProcessingService.sendMailNotification(
                                        user.email(),
                                        "New Medical Visit Created",
                                        String.format("A new medical visit has been created by %s on %s. Please check your appointments.",
                                                medicalVisitRequestDTO.doctorName(), medicalVisitRequestDTO.visitDate())
                                );
                            }

                            asyncProcessingService.sendAppNotification(
                                user.id(),
                                "New Medical Visit Created",
                                String.format("A new medical visit has been created by %s on %s. Please check your appointments.",
                                        medicalVisitRequestDTO.doctorName(), medicalVisitRequestDTO.visitDate()),
                                medVisit.getId(),
                                "/appointments"
                            );
                        }

                );
    }

    @Override
    @Transactional
    public void updateMedicalVisit(MedicalVisitRequestDTO medicalVisitRequestDTO, Long medicalVisitId) {
        // Validate request
        validateMedicalVisitRequest(medicalVisitRequestDTO);

        MedicalVisit medicalVisit = medicalVisitRepository.findById(medicalVisitId)
                .orElseThrow(() -> new MedicalVisitException("Medical visit not found"));

        // Check for conflicts with other visits (excluding current visit)
        if (medicalVisitRepository.existsByDoctorNameAndVisitDateAndIdNot(
                medicalVisitRequestDTO.doctorName(), medicalVisitRequestDTO.visitDate(), medicalVisitId)) {
            throw new MedicalVisitException("Another medical visit already exists for the same doctor and date");
        }

        // Update fields
        medicalVisit.setDoctorName(medicalVisitRequestDTO.doctorName());
        medicalVisit.setVisitDate(medicalVisitRequestDTO.visitDate());
        medicalVisit.setStartTime(medicalVisitRequestDTO.startTime());
        medicalVisit.setEndTime(medicalVisitRequestDTO.endTime());
        medicalVisitRepository.save(medicalVisit);
    }

    @Override
    @Transactional
    public void deleteMedicalVisit(Long id) {
        if (!medicalVisitRepository.existsById(id)) {
            throw new MedicalVisitException("Medical visit not found");
        }
        MedicalVisit medicalVisit = medicalVisitRepository.findById(id)
                .orElseThrow(() -> new MedicalVisitException("Medical visit not found"));
        medicalVisitRepository.deleteById(id);
    }

    @Override
    public MedicalVisitResponseDTO getMedicalVisit(Long id) {
        return medicalVisitRepository.findById(id)
                .map(
                        medicalVisit -> {
                            MedicalVisitResponseDTO medicalVisitResponseDTO = medicalVisitMapper.toDto(medicalVisit);
                            final boolean didIBookVisit = medicalVisit.getAppointments().stream()
                                    .anyMatch(appointment -> appointment.getUserId().equals(getCurrentUser().id()));
                            medicalVisitResponseDTO.setDidIBookVisit(didIBookVisit);
                            return medicalVisitResponseDTO;
                        }
                )
                .orElseThrow(() -> new MedicalVisitException("Medical visit not found"));
    }

    @Override
    public List<MedicalVisitResponseDTO> getMedicalVisits() {
        return medicalVisitRepository.findAll().stream()
                .map(
                        medicalVisit -> {
                            MedicalVisitResponseDTO medicalVisitResponseDTO =medicalVisitMapper.toDto(medicalVisit);
                                    final boolean didIBookVisit = medicalVisit.getAppointments().stream()
                                            .anyMatch(appointment -> appointment.getUserId().equals(getCurrentUser().id()));
                            medicalVisitResponseDTO.setDidIBookVisit(didIBookVisit);
                            return medicalVisitResponseDTO;
                        }
                )
                .toList();
    }

    private void validateMedicalVisitRequest(MedicalVisitRequestDTO request) {
        if (request.doctorName() == null || request.doctorName().isBlank()) {
            throw new MedicalVisitException("Doctor name cannot be empty");
        }
        if (request.visitDate() == null) {
            throw new MedicalVisitException("Visit date cannot be null");
        }
        if (request.startTime() == null || request.endTime() == null) {
            throw new MedicalVisitException("Start and end times cannot be null");
        }
        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())) {
            throw new MedicalVisitException("Start time must be before end time");
        }
    }


    private UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserResponse userResponse) {
            return userResponse;
        }
        throw new AppointmentException( String.format("Current user is not authenticated or does not have a valid user response: %s", principal));
    }
}