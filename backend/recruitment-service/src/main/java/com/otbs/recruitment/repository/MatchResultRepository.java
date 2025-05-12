package com.otbs.recruitment.repository;

import com.otbs.recruitment.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
    // Custom query methods can be defined here if needed
}
