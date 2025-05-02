package com.otbs.medVisit.controller;

import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.dto.MessageResponseDTO;
import com.otbs.medVisit.service.MedicalVisitService;
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

@RequestMapping("/api/v1/medical-visits")
@RequiredArgsConstructor
@Slf4j
@RestController
@Tag(name = "Medical Visit Management", description = "APIs for managing medical visits")
@SecurityRequirement(name = "bearerAuth")
public class MedicalVisitController {

    private final MedicalVisitService medicalVisitService;

    @Operation(
            summary = "Get all medical visits",
            description = "Retrieves a list of all medical visits. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of medical visits",
            content = @Content(schema = @Schema(implementation = MedicalVisitResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<MedicalVisitResponseDTO>> getMedicalVisits() {
        return ResponseEntity.ok(medicalVisitService.getMedicalVisits());
    }

    @Operation(
            summary = "Get medical visit by ID",
            description = "Retrieves details of a specific medical visit by ID. Requires HR role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Medical visit found",
            content = @Content(schema = @Schema(implementation = MedicalVisitResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Medical visit not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MedicalVisitResponseDTO> getMedicalVisit(
            @Parameter(description = "ID of the medical visit", example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(medicalVisitService.getMedicalVisit(id));
    }

    @Operation(
            summary = "Create a new medical visit",
            description = "Creates a new medical visit with the specified details. Requires HR role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Medical visit created successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., end time before start time)")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PostMapping
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> createMedicalVisit(
            @Valid @RequestBody MedicalVisitRequestDTO medicalVisitRequestDTO
    ) {
        medicalVisitService.createMedicalVisit(medicalVisitRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Medical visit created successfully"));
    }

    @Operation(
            summary = "Update a medical visit",
            description = "Updates an existing medical visit with the specified details. Requires HR role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Medical visit updated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., end time before start time)")
    @ApiResponse(responseCode = "404", description = "Medical visit not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> updateMedicalVisit(
            @Parameter(description = "ID of the medical visit", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody MedicalVisitRequestDTO medicalVisitRequestDTO
    ) {
        medicalVisitService.updateMedicalVisit(medicalVisitRequestDTO, id);
        return ResponseEntity.ok(new MessageResponseDTO("Medical visit updated successfully"));
    }

    @Operation(
            summary = "Delete a medical visit",
            description = "Deletes an existing medical visit by ID. Requires HR role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Medical visit deleted successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Medical visit not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> deleteMedicalVisit(
            @Parameter(description = "ID of the medical visit", example = "1")
            @PathVariable("id") Long id
    ) {
        medicalVisitService.deleteMedicalVisit(id);
        return ResponseEntity.ok(new MessageResponseDTO("Medical visit deleted successfully"));
    }
}