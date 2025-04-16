package com.otbs.training.dto;

import java.time.LocalDate;

public record TrainingRequestDTO(
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}
