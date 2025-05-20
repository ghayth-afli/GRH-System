package com.otbs.recruitment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "match_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score")
    private double score;

    @Column(name = "raw_score")
    @JsonProperty("raw_score")
    private int rawScore;

    @Column(name = "interpretation")
    private String interpretation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "details_id")
    private MatchScoreDetails details;

    @ElementCollection
    @CollectionTable(name = "match_result_red_flags", joinColumns = @JoinColumn(name = "match_result_id"))
    @Column(name = "red_flag")
    @JsonProperty("red_flags")
    private List<String> redFlags;

    @ElementCollection
    @CollectionTable(name = "match_result_bonus_points", joinColumns = @JoinColumn(name = "match_result_id"))
    @Column(name = "bonus_point")
    @JsonProperty("bonus_points")
    private List<String> bonusPoints;

    @Column(name = "role_type")
    @JsonProperty("role_type")
    private String roleType;

    @Column(name = "role_confidence")
    @JsonProperty("role_confidence")
    private double roleConfidence;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "match_result_id")
    @JsonProperty("adapted_criteria")
    private List<Criterion> adaptedCriteria;

    @ElementCollection
    @CollectionTable(name = "match_result_role_insights", joinColumns = @JoinColumn(name = "match_result_id"))
    @Column(name = "role_insight",length = 1000)
    @JsonProperty("role_specific_insights")
    private List<String> roleSpecificInsights;


    @Entity
    @Table(name = "criterion")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Criterion {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "name")
        private String name;

        @Column(name = "weight")
        private int weight;
    }
}
