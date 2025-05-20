package com.otbs.recruitment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_offer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    @Column(name = "department_name")
    private String department;

    @Size(max = 2000, message = "Responsibilities must not exceed 2000 characters")
    private String responsibilities;

    @Size(max = 2000, message = "Qualifications must not exceed 2000 characters")
    private String qualifications;

    @Size(max = 100, message = "Role name must not exceed 100 characters")
    @Column(name = "role_name")
    private String role;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EJobOfferStatus status;

    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal;

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InternalApplication> internalApplications;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
