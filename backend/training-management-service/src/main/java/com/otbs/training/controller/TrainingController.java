package com.otbs.training.controller;

import com.otbs.training.dto.MessageResponseDTO;
import com.otbs.training.dto.TrainingRequestDTO;
import com.otbs.training.dto.TrainingResponseDTO;
import com.otbs.training.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Training Management", description = "APIs for managing training sessions")
@SecurityRequirement(name = "bearerAuth")
public class TrainingController {

    private final TrainingService trainingService;

    @Operation(
            summary = "Create a new training session",
            description = "Creates a new training session with the specified details. Requires Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Training session created successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., end date before start date)")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PostMapping
    @PreAuthorize("hasAuthority('Manager') ")
    public ResponseEntity<MessageResponseDTO> createTraining(
            @Valid @RequestBody TrainingRequestDTO trainingRequestDTO
    ) {
        trainingService.createTraining(trainingRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Training created successfully"));
    }

    @Operation(
            summary = "Update a training session",
            description = "Updates an existing training session with the specified details. Requires Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Training session updated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., end date before start date)")
    @ApiResponse(responseCode = "404", description = "Training session not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> updateTraining(
            @Valid @RequestBody TrainingRequestDTO trainingRequestDTO,
            @Parameter(description = "ID of the training session", example = "1")
            @PathVariable("id") Long id
    ) {
        trainingService.updateTraining(trainingRequestDTO, id);
        return ResponseEntity.ok(new MessageResponseDTO("Training updated successfully"));
    }

    @Operation(
            summary = "Delete a training session",
            description = "Deletes an existing training session by ID. Requires Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Training session deleted successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Training session not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> deleteTraining(
            @Parameter(description = "ID of the training session", example = "1")
            @PathVariable("id") Long id
    ) {
        trainingService.deleteTraining(id);
        return ResponseEntity.ok(new MessageResponseDTO("Training deleted successfully"));
    }

    @Operation(
            summary = "Get all training sessions",
            description = "Retrieves a list of all training sessions. Requires HR, Manager, or User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of training sessions",
            content = @Content(schema = @Schema(implementation = TrainingResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Manager') or hasAuthority('Employee') or hasAuthority('HRD')")
    public ResponseEntity<List<TrainingResponseDTO>> getAllTrainings() {
        return ResponseEntity.ok(trainingService.getAllTrainings());
    }

    @Operation(
            summary = "Get training session by ID",
            description = "Retrieves details of a specific training session by ID. Requires HR or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Training session found",
            content = @Content(schema = @Schema(implementation = TrainingResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Training session not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Manager') or hasAuthority('Employee')  or hasAuthority('HRD')")
    public ResponseEntity<TrainingResponseDTO> getTrainingById(
            @Parameter(description = "ID of the training session", example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(trainingService.getTrainingById(id));
    }
}