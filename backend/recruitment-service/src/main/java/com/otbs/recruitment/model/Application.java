package com.otbs.recruitment.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"resume", "coverLetter"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offer_id", nullable = false)
    private JobOffer jobOffer;

    @NotBlank(message = "Applicant identifier cannot be blank")
    @Column(name = "applicant_identifier", nullable = false, length = 100)
    private String applicantIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false)
    private EApplicantType applicantType;

    @Lob
    @Column(name = "resume")
    private byte[] resume;

    @Lob
    @Column(name = "cover_letter")
    private byte[] coverLetter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EApplicationStatus status;

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

