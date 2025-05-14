package com.otbs.recruitment.repository;

import com.otbs.recruitment.model.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> , JpaSpecificationExecutor<JobOffer> {
    //findByIdAndCreatedBy
    Optional<JobOffer> findByIdAndCreatedBy(Long id, String createdBy);

}

