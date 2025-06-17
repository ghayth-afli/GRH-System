package com.otbs.training.controller;

import com.otbs.training.dto.InvitationResponseDTO;
import com.otbs.training.dto.MessageResponseDTO;
import com.otbs.training.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invitation Management", description = "APIs for managing training invitations")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    @Operation(
            summary = "Confirm a training invitation",
            description = "Confirms a training invitation by ID. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Invitation confirmed successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Invitation not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<MessageResponseDTO> confirmInvitation(
            @Parameter(description = "ID of the invitation", example = "1")
            @PathVariable("id") Long id
    ) {
        invitationService.confirmInvitation(id);
        return ResponseEntity.ok(new MessageResponseDTO("Invitation confirmed successfully"));
    }

    @Operation(
            summary = "Get all training invitations",
            description = "Retrieves a list of all training invitations for the authenticated user. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of invitations",
            content = @Content(schema = @Schema(implementation = InvitationResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<List<InvitationResponseDTO>> getAllInvitations() {
        return ResponseEntity.ok(invitationService.getAllInvitations());
    }

    //get all invitations by training id
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('HRD')")
    @GetMapping("/training/{trainingId}")
    public ResponseEntity<List<InvitationResponseDTO>> getAllInvitationsByTrainingId(
            @Parameter(description = "ID of the training session", example = "1")
            @PathVariable("trainingId") Long trainingId) {
        return ResponseEntity.ok(invitationService.getAllInvitationsByTrainingId(trainingId));
    }
}