package com.otbs.recruitment.controller;

import com.otbs.recruitment.dto.ApplicationDetailsResponseDTO;
import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.model.EApplicationStatus;
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

    @PostMapping("/job-offer/{jobOfferId}")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<Void> createApplication(
            @PathVariable("jobOfferId") Long jobOfferId,
            @RequestParam("resume") MultipartFile resume) {
        applicationService.createApplication(jobOfferId, resume);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAuthority('HR') or hasAuthority('HRD')")
    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> deleteApplication(@PathVariable("applicationId") Long applicationId) {
        applicationService.deleteApplication(applicationId);
        return ResponseEntity.noContent().build();
    }

    //cancel application
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager')")
    @DeleteMapping("/cancel/{jobOfferId}")
    public ResponseEntity<Void> cancelApplication(@PathVariable("jobOfferId") Long jobOfferId) {
        applicationService.deleteApplicationByJobOfferId(jobOfferId);
        return ResponseEntity.noContent().build();
    }




    @PreAuthorize("hasAuthority('HR') or hasAuthority('Manager') or hasAuthority('HRD')")
    @GetMapping("/job-offer/{jobId}")
    public ResponseEntity<List<ApplicationResponseDTO>> getAllApplications(
            @PathVariable("jobId") Long jobId
    ) {
        List<ApplicationResponseDTO> applications = applicationService.getAllApplications(jobId);
        return ResponseEntity.ok(applications);
    }

    @PreAuthorize("hasAuthority('HR') or hasAuthority('Manager') or hasAuthority('HRD')")
    @GetMapping("/details/{applicationId}")
    public ResponseEntity<ApplicationDetailsResponseDTO> getApplicationDetails(@PathVariable("applicationId") Long applicationId) {
        ApplicationDetailsResponseDTO response = applicationService.getApplicationDetails(applicationId);
        return ResponseEntity.ok(response);
    }

    //change application status
    @PreAuthorize("hasAuthority('HR') or hasAuthority('HRD')")
    @PutMapping("/{applicationId}/status")
    public ResponseEntity<Void> updateApplicationStatus(
            @PathVariable("applicationId") Long applicationId,
            @RequestParam("status") String status
    ) {
        applicationService.updateApplicationStatus(applicationId, EApplicationStatus.valueOf(status));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
