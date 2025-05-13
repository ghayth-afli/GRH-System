package com.otbs.recruitment.repository;

import com.otbs.recruitment.model.InternalApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InternalApplicationRepository extends JpaRepository<InternalApplication, Long> {
    //findByIdAndEmployeeId
    Optional<InternalApplication> findByIdAndEmployeeId(Long id, String employeeId);
    List<InternalApplication> findAllByJobOfferId(Long jobOffer_id);
}

