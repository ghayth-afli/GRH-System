package com.otbs.recruitment.repository;

import com.otbs.recruitment.model.InternalApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InternalApplicationRepository extends JpaRepository<InternalApplication, Long> {
    //findByIdAndUserId
    Optional<InternalApplication> findByIdAndUserId(Long id, String userId);
    List<InternalApplication> findAllByJobOfferId(Long jobOffer_id);
    //findByJobOfferIdAndUserId
    Optional<InternalApplication> findByJobOfferIdAndUserId(Long jobOfferId, String userId);
}

