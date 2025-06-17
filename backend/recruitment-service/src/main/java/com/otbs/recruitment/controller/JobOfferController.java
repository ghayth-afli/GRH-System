package com.otbs.recruitment.controller;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.model.EJobOfferStatus;
import com.otbs.recruitment.service.JobOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private static final Logger log = LoggerFactory.getLogger(JobOfferController.class);
    private final JobOfferService jobOfferService;

    @PostMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<Void> createJobOffer(@Valid @RequestBody JobOfferRequestDTO jobOfferRequestDTO) {
       jobOfferService.createJobOffer(jobOfferRequestDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<Void> updateJobOffer(@PathVariable("id") Long id, @Valid @RequestBody JobOfferRequestDTO jobOfferRequestDTO) {
        jobOfferService.updateJobOffer(id, jobOfferRequestDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable("id") Long id) {
        jobOfferService.deleteJobOffer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<JobOfferResponseDTO> getJobOfferById(@PathVariable("id") Long id) {
        JobOfferResponseDTO jobOffer = jobOfferService.getJobOfferById(id);
        return new ResponseEntity<>(jobOffer, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<List<JobOfferResponseDTO>> getAllJobOffers(){
        return new ResponseEntity<>(jobOfferService.getAllJobOffers(), HttpStatus.OK);
    }

    //toggle job offer status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<Void> toggleJobOfferStatus(@PathVariable("id") Long id
                , @RequestParam("status") String status
    ) {
        log.info("Toggling job offer status for ID: {} to status: {}", id, status);
        jobOfferService.toggleStatus(id, EJobOfferStatus.valueOf(status));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}