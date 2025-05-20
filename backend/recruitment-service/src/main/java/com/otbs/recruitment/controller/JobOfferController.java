package com.otbs.recruitment.controller;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.model.EApplicationStatus;
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
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Void> createJobOffer(@Valid @RequestBody JobOfferRequestDTO jobOfferRequestDTO) {
       jobOfferService.createJobOffer(jobOfferRequestDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Void> updateJobOffer(@PathVariable("id") Long id, @Valid @RequestBody JobOfferRequestDTO jobOfferRequestDTO) {
        jobOfferService.updateJobOffer(id, jobOfferRequestDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Void> deleteJobOffer(@PathVariable("id") Long id) {
        jobOfferService.deleteJobOffer(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<JobOfferResponseDTO> getJobOfferById(@PathVariable("id") Long id) {
        JobOfferResponseDTO jobOffer = jobOfferService.getJobOfferById(id);
        return new ResponseEntity<>(jobOffer, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<List<JobOfferResponseDTO>> getAllJobOffers(){
        return new ResponseEntity<>(jobOfferService.getAllJobOffers(), HttpStatus.OK);
    }

    //toggle job offer status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Void> toggleJobOfferStatus(@PathVariable("id") Long id
                , @RequestParam("status") String status
    ) {
        log.info("Toggling job offer status for ID: {} to status: {}", id, status);
        jobOfferService.toggleStatus(id, EJobOfferStatus.valueOf(status));
        return new ResponseEntity<>(HttpStatus.OK);
    }

//    @GetMapping
//    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
//    public ResponseEntity<Map<String, Object>> getAllJobOffers(
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size,
//            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
//            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir,
//            @RequestParam(value = "title", required = false) String title,
//            @RequestParam(value = "department", required = false) String department,
//            @RequestParam(value = "role", required = false) String role,
//            @RequestParam(value = "isInternal", required = false) Boolean isInternal,
//            @RequestParam(value = "status", required = false) String status) {
//
//        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
//
//        Page<JobOfferResponseDTO> jobOffersPage = jobOfferService.getAllJobOffers(
//                title, department, role, isInternal, status, pageable);
//        List<JobOfferResponseDTO> jobOffers = jobOffersPage.getContent();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("content", jobOffers);
//        response.put("currentPage", jobOffersPage.getNumber());
//        response.put("totalItems", jobOffersPage.getTotalElements());
//        response.put("totalPages", jobOffersPage.getTotalPages());
//
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }
}