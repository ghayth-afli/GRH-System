package com.otbs.recruitment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "external_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @NotBlank(message = "Applicant identifier cannot be blank")
    @Column(name = "candidate_id", nullable = false, length = 100)
    private Long candidateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EApplicationStatus status = EApplicationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    public void markAsSelected() {
        this.status = EApplicationStatus.SELECTED;
    }

    public void markAsRejected() {
        this.status = EApplicationStatus.REJECTED;
    }
}
