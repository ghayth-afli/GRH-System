package com.otbs.recruitment.controller;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.service.JobOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/job-offers")
@RequiredArgsConstructor
public class JobOfferController {

    private final JobOfferService jobOfferService;

    @PostMapping
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<JobOfferResponseDTO> createJobOffer(@Valid @RequestBody JobOfferRequestDTO jobOfferRequestDTO) {
        JobOfferResponseDTO createdJobOffer = jobOfferService.createJobOffer(jobOfferRequestDTO);
        return new ResponseEntity<>(createdJobOffer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<JobOfferResponseDTO> updateJobOffer(@PathVariable Long id, @Valid @RequestBody JobOfferRequestDTO jobOfferRequestDTO) {
        JobOfferResponseDTO updatedJobOffer = jobOfferService.updateJobOffer(id, jobOfferRequestDTO);
        return new ResponseEntity<>(updatedJobOffer, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable Long id) {
        jobOfferService.deleteJobOffer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<JobOfferResponseDTO> getJobOfferById(@PathVariable Long id) {
        JobOfferResponseDTO jobOffer = jobOfferService.getJobOfferById(id);
        return new ResponseEntity<>(jobOffer, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<List<JobOfferResponseDTO>> getAllJobOffers() {
        List<JobOfferResponseDTO> jobOffers = jobOfferService.getAllJobOffers();
        return new ResponseEntity<>(jobOffers, HttpStatus.OK);
    }
}
