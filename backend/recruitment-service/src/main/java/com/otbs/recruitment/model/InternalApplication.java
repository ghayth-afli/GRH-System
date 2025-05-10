package com.otbs.recruitment.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "internal_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Applicant identifier cannot be blank")
    @Column(name = "candidate_id", nullable = false, length = 100)
    private Long candidateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EApplicationStatus status = EApplicationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    @OneToOne
    @JoinColumn(name = "match_result_id", referencedColumnName = "id")
    private MatchResult matchResult;

    private String employeeId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void markAsSelected() {
        this.status = EApplicationStatus.SELECTED;
    }

    public void markAsRejected() {
        this.status = EApplicationStatus.REJECTED;
    }
}
