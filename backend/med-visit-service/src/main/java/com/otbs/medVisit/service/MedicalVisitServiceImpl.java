package com.otbs.medVisit.service;

import com.otbs.common.event.Event;
import com.otbs.feign.client.EmployeeClient;
import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.exception.MedicalVisitException;
import com.otbs.medVisit.mapper.MedicalVisitMapper;
import com.otbs.medVisit.model.MedicalVisit;
import com.otbs.medVisit.repository.MedicalVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
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
public class MedicalVisitServiceImpl implements MedicalVisitService {

    private final MedicalVisitRepository medicalVisitRepository;
    private final MedicalVisitMapper medicalVisitMapper;
    private final EmployeeClient employeeClient;
    private final MedicalVisitNotificationService notificationService;
    private final MedicalVisitNotificationEventService notificationEventService;


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

        // Asynchronously notify employees
        notifyEmployeesAsync(medicalVisitRequestDTO, medVisit.getId());
        sendAsyncEventNotifications(medVisit, "CREATED_MEDICAL_VISIT");
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
        sendAsyncEventNotifications(medicalVisit, "UPDATED_MEDICAL_VISIT");
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
        sendAsyncEventNotifications(medicalVisit, "DELETED_MEDICAL_VISIT");
    }

    @Override
    public MedicalVisitResponseDTO getMedicalVisit(Long id) {
        return medicalVisitRepository.findById(id)
                .map(medicalVisitMapper::toDto)
                .orElseThrow(() -> new MedicalVisitException("Medical visit not found"));
    }

    @Override
    public List<MedicalVisitResponseDTO> getMedicalVisits() {
        return medicalVisitRepository.findAll().stream()
                .map(medicalVisitMapper::toDto)
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

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncEventNotifications(MedicalVisit medVisit, String eventType) {
        MedicalVisitResponseDTO medicalVisitResponseDTO = medicalVisitMapper.toDto(medVisit);
        Map<String, Object> payload = new HashMap<>();
        payload.put("medicalVisit", medicalVisitResponseDTO);
        CompletableFuture<Void> eventNotification = CompletableFuture.runAsync(() ->
                notificationEventService.sendEventNotification(
                        new Event(
                                eventType,
                                medVisit.getId().toString(),
                                "MEDICAL_VISIT",
                                payload
                        )
                ));

        CompletableFuture.allOf(eventNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send event notifications for leave ID {}: {}",
                                medVisit.getId(), throwable.getMessage());
                    } else {
                        log.debug("Event notifications sent successfully for leave ID {}", medVisit.getId());
                    }
                });
    }

    @Async
    protected void notifyEmployeesAsync(MedicalVisitRequestDTO medicalVisitRequestDTO, Long medicalVisitId) {
        CompletableFuture.supplyAsync(employeeClient::getAllEmployees)
                .thenAccept(employees -> employees.forEach(employee -> {
                    String message = String.format(
                            "A new medical visit has been scheduled with %s on %s from %s to %s",
                            medicalVisitRequestDTO.doctorName(),
                            medicalVisitRequestDTO.visitDate(),
                            medicalVisitRequestDTO.startTime(),
                            medicalVisitRequestDTO.endTime());

                    CompletableFuture.allOf(
                            CompletableFuture.runAsync(() -> notificationService.sendMedicalVisitNotification(
                                    employee.id(), "New Medical Visit", message, medicalVisitId)),
                            CompletableFuture.runAsync(() -> notificationService.sendMailNotification(
                                    employee.email(), "New Medical Visit", message))
                    ).exceptionally(throwable -> {
                        log.error("Failed to send notification to employee {}: {}", employee.id(), throwable.getMessage());
                        return null;
                    });
                }))
                .exceptionally(throwable -> {
                    log.error("Failed to fetch employees for notification: {}", throwable.getMessage());
                    return null;
                });
    }
}