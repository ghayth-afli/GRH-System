package com.otbs.medVisit.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MedicalVisitResponseDTO {
    private Long id;
    private String doctorName;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfAppointments;
    private Boolean didIBookVisit;

}