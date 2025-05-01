package com.otbs.medVisit.controller;


import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.dto.MessageResponseDTO;
import com.otbs.medVisit.service.MedicalVisitService;
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
public class MedicalVisitController {

    private final MedicalVisitService medicalVisitService;

    @GetMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<MedicalVisitResponseDTO>> getMedicalVisits() {
        return ResponseEntity.ok(medicalVisitService.getMedicalVisits());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MedicalVisitResponseDTO> getMedicalVisit(@PathVariable("id") Long id) {
        return ResponseEntity.ok(medicalVisitService.getMedicalVisit(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> createMedicalVisit(@Valid @RequestBody MedicalVisitRequestDTO medicalVisitRequestDTO) {
        medicalVisitService.createMedicalVisit(medicalVisitRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Medical visit created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> updateMedicalVisit(@PathVariable("id") Long id, @Valid @RequestBody MedicalVisitRequestDTO medicalVisitRequestDTO) {
        medicalVisitService.updateMedicalVisit(medicalVisitRequestDTO, id);
        return ResponseEntity.ok(new MessageResponseDTO("Medical visit updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> deleteMedicalVisit(@PathVariable("id") Long id) {
        medicalVisitService.deleteMedicalVisit(id);
        return ResponseEntity.ok(new MessageResponseDTO("Medical visit deleted successfully"));
    }
}
