package com.otbs.recruitment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "match_score_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScoreDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "skills_match_id")
    private MatchEvaluationDetail skillsMatch;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "relevant_experience_id")
    private MatchEvaluationDetail relevantExperience;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "education_id")
    private MatchEvaluationDetail education;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "certifications_id")
    private MatchEvaluationDetail certifications;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cultural_fit_id")
    private MatchEvaluationDetail culturalFit;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "language_proficiency_id")
    private MatchEvaluationDetail languageProficiency;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "achievements_projects_id")
    private MatchEvaluationDetail achievementsProjects;
}
