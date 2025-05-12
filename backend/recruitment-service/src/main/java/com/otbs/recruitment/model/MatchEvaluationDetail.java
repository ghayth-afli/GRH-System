package com.otbs.recruitment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "match_evaluation_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEvaluationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_score",length = 1000)
    @JsonProperty("raw_score")
    private int rawScore;

    @Column(name = "weighted_score",length = 1000)
    @JsonProperty("weighted_score")
    private double weightedScore;

    @ElementCollection
    @CollectionTable(name = "match_evaluation_matching_skills", joinColumns = @JoinColumn(name = "match_evaluation_detail_id"))
    @Column(name = "matching_skill",length = 1000)
    @JsonProperty("matching_skills")
    private List<String> matchingSkills;

    @ElementCollection
    @CollectionTable(name = "match_evaluation_missing_skills", joinColumns = @JoinColumn(name = "match_evaluation_detail_id"))
    @Column(name = "missing_skill")
    @JsonProperty("missing_skills")
    private List<String> missingSkills;

    @Column(name = "analysis",length = 1000)
    private String analysis;
}
