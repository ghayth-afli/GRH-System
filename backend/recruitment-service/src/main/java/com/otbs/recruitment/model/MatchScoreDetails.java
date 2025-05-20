package com.otbs.recruitment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("skills_match")
    private MatchEvaluationDetail skillsMatch;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "relevant_experience_id")
    @JsonProperty("relevant_experience")
    private MatchEvaluationDetail relevantExperience;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "education_id")
    private MatchEvaluationDetail education;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "certifications_id")
    private MatchEvaluationDetail certifications;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cultural_fit_id")
    @JsonProperty("cultural_fit")
    private MatchEvaluationDetail culturalFit;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "language_proficiency_id")
    @JsonProperty("language_proficiency")
    private MatchEvaluationDetail languageProficiency;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "achievements_projects_id")
    @JsonProperty("achievements_projects")
    private MatchEvaluationDetail achievementsProjects;
}
