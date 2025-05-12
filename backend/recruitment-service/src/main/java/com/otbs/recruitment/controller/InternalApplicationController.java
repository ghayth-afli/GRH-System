package com.otbs.recruitment.controller;

import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal-applications")
@RequiredArgsConstructor
public class InternalApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/{jobOfferId}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<Void> createApplication(
            @PathVariable("jobOfferId") Long jobOfferId,
            @RequestParam("resume") MultipartFile resume) {
        applicationService.createApplication(jobOfferId, resume);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAuthority('HR')")
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long applicationId) {
        applicationService.deleteApplication(applicationId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponseDTO> getApplicationById(@PathVariable Long applicationId) {
        ApplicationResponseDTO response = applicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    @GetMapping
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications() {
        List<ApplicationResponseDTO> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }
}
